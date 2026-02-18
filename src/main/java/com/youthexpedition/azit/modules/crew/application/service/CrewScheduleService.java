package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
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
    private final SaveCrewSchedulePort saveCrewSchedulePort;

    @Override
    public void createSchedule(CreateScheduleCommand command) {
        // 크루 정회원인지 확인
        CrewMember creator = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.creatorId())
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));

        //  정기런은 리더만 생성 가능
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

    // 일정 유효성 체크
    private void validateSchedule(CrewSchedule schedule) {
        // 현재보다 이후의 시간인지 검증
        if (!schedule.isMeetingTimeValid(schedule.getMeetingAt())) throw new BusinessException(CrewErrorCode.INVALID_SCHEDULE_TIME);
    }
}
