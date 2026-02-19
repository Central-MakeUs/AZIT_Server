package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateScheduleCommand;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewScheduleService implements CrewScheduleUseCase {
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewSchedulePort loadCrewSchedulePort;
    private final SaveCrewSchedulePort saveCrewSchedulePort;

    @Override
    public void createSchedule(CreateScheduleCommand command) {
        // 크루 정회원인지 확인
        CrewMember creator = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.creatorId())
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));

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
        CrewSchedule schedule = loadCrewSchedulePort.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.SCHEDULE_NOT_FOUND));

        // 본인이 생성한 일정인지 확인
        if (!schedule.getCreatorId().equals(command.creatorId())) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }

        // 크루 정회원인지 확인
        CrewMember creator = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.creatorId())
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));

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
        CrewSchedule schedule = loadCrewSchedulePort.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.SCHEDULE_NOT_FOUND));

        // 이미 취소된 일정인지 확인
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }

        // 본인이 생성한 일정인지 확인
        if (!schedule.getCreatorId().equals(command.creatorId())) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }

        // 크루 정회원인지 확인
        loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.creatorId())
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));

        // 삭제 처리
        schedule.cancel();

        saveCrewSchedulePort.save(schedule);
    }


    // 일정 유효성 체크
    private void validateSchedule(CrewSchedule schedule) {
        // 현재보다 이후의 시간인지 검증
        if (!schedule.isMeetingTimeValid()) throw new BusinessException(CrewErrorCode.INVALID_SCHEDULE_TIME);
    }
}
