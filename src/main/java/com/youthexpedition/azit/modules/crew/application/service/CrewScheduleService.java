package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CrewScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleMonthlyListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.ParticipantResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleMonthlyQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleQuery;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.application.service.dto.ScheduleData;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewScheduleResponseMapper;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.Location;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CheckInStatusResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrewScheduleService implements CrewScheduleUseCase {
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewSchedulePort loadCrewSchedulePort;
    private final SaveCrewSchedulePort saveCrewSchedulePort;
    private final LoadMemberPort loadMemberPort;
    private final CrewScheduleResponseMapper crewScheduleResponseMapper;

    @Override
    public void createSchedule(CreateScheduleCommand command) {
        // 크루 정회원인지 확인
        CrewMember creator = getJoinedMember(command.crewId(), command.creatorId());

        // 정기런은 리더만 생성 가능
        if (command.runType() == RunType.REGULAR && creator.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.ONLY_LEADER_CAN_CREATE_REGULAR_RUN);
        }

        Location location = Location.builder()
                .placeName(command.placeName())
                .address(command.address())
                .meetingSpot(command.meetingSpot())
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

                .placeName(command.placeName())
                .address(command.address())
                .meetingSpot(command.meetingSpot())
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
        ScheduleData data = getValidatedScheduleData(command);

        return crewScheduleResponseMapper.toDetailResponse(
                data.schedule(), command.memberId(), data.profileMap(), data.crewMemberMap());
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ParticipantResponse> getScheduleParticipants(CrewScheduleCommand command, CursorPageQuery query) {
        ScheduleData data = getValidatedScheduleData(command);

        return crewScheduleResponseMapper.toParticipantSliceResponse(
                data.schedule(), data.profileMap(), data.crewMemberMap(), query);
    }

    // 일정 상세 및 참여자 명단 조회 시 공통적으로 사용하는 검증 및 데이터 로딩 로직
    private ScheduleData getValidatedScheduleData(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 취소된 일정인지 확인
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        List<Long> participantIds = schedule.getParticipantIds();
        Map<Long, MemberProfileDto> profileMap = loadMemberPort.findAllByIds(participantIds);
        Map<Long, CrewMember> crewMemberMap = loadCrewMemberPort.findAllByCrewIdAndMemberIds(command.crewId(), participantIds);

        // 데이터 정합성 검증
        if (profileMap.size() != participantIds.size() || crewMemberMap.size() != participantIds.size()) {
            log.warn("Data inconsistency detected for schedule {}: participants={}, profiles={}, crewMembers={}",
                    schedule.getId(), participantIds.size(), profileMap.size(), crewMemberMap.size());
        }

        return new ScheduleData(schedule, profileMap, crewMemberMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleListResponse> getSchedules(CrewScheduleQuery query) {
        // 정회원인지 확인
        getJoinedMember(query.crewId(), query.memberId());

        // 필터링된 일정 목록 조회
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByFilter(query.crewId(), query.date(), query.runType());

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, query.memberId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleMonthlyListResponse> getMonthlySchedulesForCalendar(CrewScheduleMonthlyQuery query) {
        // 정회원인지 확인
        getJoinedMember(query.crewId(), query.memberId());

        Map<LocalDate, Set<RunType>> monthlyScheduleMap = loadCrewSchedulePort.findMonthlySchedulesForCalendar(query.crewId(), query.yearMonth());

        return crewScheduleResponseMapper.toScheduleMonthlyListResponse(monthlyScheduleMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleListResponse> getMySchedules(Long memberId) {
        // 참여 중인 일정 조회
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByMemberId(memberId);

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, memberId);
    }

    @Transactional(readOnly = true)
    @Override
    public CheckInStatusResponse getCheckInStatus(Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 오늘 참여하는 모든 일정 조회
        List<CrewSchedule> todaySchedules = loadCrewSchedulePort.findAllTodaySchedulesByMemberId(memberId, now);

        // 가장 가까운 미래 일정 조회
        Optional<CrewSchedule> nextSchedule = loadCrewSchedulePort.findNextClosestScheduleByMemberId(memberId, now);

        return crewScheduleResponseMapper.toCheckInStatusResponse(todaySchedules, nextSchedule, memberId);
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
