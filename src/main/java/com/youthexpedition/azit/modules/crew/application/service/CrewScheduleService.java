package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CrewScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.Location;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewScheduleService implements CrewScheduleUseCase {
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewSchedulePort loadCrewSchedulePort;
    private final SaveCrewSchedulePort saveCrewSchedulePort;
    private final LoadMemberPort loadMemberPort;

    @Override
    public void createSchedule(CreateScheduleCommand command) {
        // 크루 정회원인지 확인
        CrewMember creator = getJoinedMember(command.crewId(), command.creatorId());

        // 정기런은 리더만 생성 가능
        if (command.runType() == RunType.REGULAR && creator.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.ONLY_LEADER_CAN_CREATE_REGULAR_RUN);
        }

        Location location = Location.builder()
                .name(command.locationName())
                .address(command.address())
                .detailedLocation(command.detailedLocation())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .build();

        CrewSchedule crewSchedule = CrewSchedule.create(
                command.crewId(),
                command.creatorId(),
                command.title(),
                command.runType(),
                command.meetingAt(),
                location,
                command.description(),
                command.distance(),
                command.pace(),
                command.maxParticipants(),
                command.supplies()
        );

        // 유효성 체크
        validateSchedule(crewSchedule);
        saveCrewSchedulePort.save(crewSchedule);
    }

    @Override
    public void updateSchedule(UpdateScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 본인이 생성한 일정인지 확인
        validateCreator(schedule, command.creatorId());

        // 크루 정회원인지 확인
        CrewMember creator = getJoinedMember(command.crewId(), command.creatorId());

        // 정기런은 리더만 생성 가능
        if (command.runType() == RunType.REGULAR && creator.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.ONLY_LEADER_CAN_CREATE_REGULAR_RUN);
        }

        Location location = Location.builder()
                .name(command.locationName())
                .address(command.address())
                .detailedLocation(command.detailedLocation())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .build();

        schedule.update(
                command.title(), command.runType(), command.meetingAt(), location,
                command.description(), command.distance(), command.pace(),
                command.maxParticipants(), command.supplies()
        );

        // 유효성 체크
        validateSchedule(schedule);
        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void cancelSchedule(CancelScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 이미 취소된 일정인지 확인
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }

        // 본인이 생성한 일정인지 확인
        validateCreator(schedule, command.creatorId());

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.creatorId());

        // 삭제 처리
        schedule.cancel();

        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void participateSchedule(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        // 일정 참여 가능한지 검증
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }
        if (schedule.isParticipating(command.memberId())) {
            throw new BusinessException(CrewErrorCode.ALREADY_PARTICIPATED);
        }
        if (schedule.isFull()) {
            throw new BusinessException(CrewErrorCode.EXCEEDED_MAX_PARTICIPANTS);
        }

        schedule.addParticipant(command.memberId());
        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void cancelParticipation(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 참여 중인지 확인
        if (!schedule.isParticipating(command.memberId())) {
            throw new BusinessException(CrewErrorCode.NOT_PARTICIPATING_SCHEDULE);
        }

        // 본인이 일정 생성자인지 확인
        if (schedule.getCreatorId().equals(command.memberId())) {
            throw new BusinessException(CrewErrorCode.CREATOR_CANNOT_CANCEL_PARTICIPATION);
        }

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        schedule.removeParticipant(command.memberId());
        saveCrewSchedulePort.save(schedule);
    }

    @Transactional(readOnly = true)
    @Override
    public CrewScheduleDetailResponse getScheduleDetail(CrewScheduleCommand command) {
        // 1. 일정 정보 조회 (Fetch Join이 적용된 Port 사용)
        CrewSchedule schedule = getScheduleOrThrow(scheduleId);
        List<Long> participantIds = schedule.getParticipantIds();

        // 2. 외부 정보 조회 (N+1 방지를 위해 IN 쿼리 기반 Batch 조회)
        // [Member 모듈] 닉네임, 프로필 이미지 정보
        Map<Long, MemberProfile> profileMap = loadMemberProfilePort.findAllByIds(participantIds);
        // [Crew 모듈] 크루 내 역할(LEADER/MEMBER) 정보
        Map<Long, CrewMember> crewMemberMap = loadCrewMemberPort.findAllByCrewIdAndMemberIds(crewId, participantIds);

        // 3. 참여자 리스트 생성 및 정렬
        List<CrewScheduleDetailResponse.ParticipantResponse> participants = participantIds.stream()
                .map(id -> {
                    MemberProfile profile = profileMap.get(id);
                    CrewMember cm = crewMemberMap.get(id);
                    // DTO 내부에 구현된 정적 팩토리 메서드 활용
                    return CrewScheduleDetailResponse.ParticipantResponse.of(
                            id,
                            profile.nickname(),
                            profile.profileImageUrl(),
                            cm.getRole(),
                            id.equals(schedule.getCreatorId()) // 일정 생성자 여부 판단
                    );
                })
                // ⭐️ 리더(LEADER)를 최상단(0순위)으로 정렬
                .sorted(Comparator.comparing(p -> p.role() == CrewMemberRole.LEADER ? 0 : 1))
                .toList();

        // 4. 최종 응답 DTO 조립 (정적 팩토리 메서드 활용)
        // 내부적으로 LocationInfoResponse.of()를 호출하여 장소 정보까지 자동 매핑합니다.
        return CrewScheduleDetailResponse.of(schedule, currentMemberId, participants);
    }

    // 일정이 존재하는지 확인
    private CrewSchedule getSchedule(Long scheduleId) {
        return loadCrewSchedulePort.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 크루 정회원인지 확인
    private CrewMember getJoinedMember(Long crewId, Long memberId) {
        return loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));
    }

    // 해당 일정의 생성자인지 확인
    private void validateCreator(CrewSchedule schedule, Long memberId) {
        if (!schedule.getCreatorId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }
    }

    // 일정 유효성 체크
    private void validateSchedule(CrewSchedule schedule) {
        // 현재보다 이후의 시간인지 검증
        if (!schedule.isMeetingTimeValid()) throw new BusinessException(CrewErrorCode.INVALID_SCHEDULE_TIME);
    }
}
