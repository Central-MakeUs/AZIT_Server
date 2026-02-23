package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CrewScheduleListResponse(
        @Schema(description = "일정 ID")
        Long scheduleId,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "러닝 타입")
        RunType runType,
        @Schema(description = "모임 시간")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingAt,
        @Schema(description = "집합 장소명")
        String placeName,
        @Schema(description = "목표 거리 (km)")
        Integer distance,
        @Schema(description = "목표 페이스")
        Integer pace,
        @Schema(description = "최대 인원")
        Integer maxParticipants,
        @Schema(description = "현재 참여 인원")
        Integer currentParticipants,
        @Schema(description = "내가 생성한 일정인지 여부")
        boolean isMine,
        @Schema(description = "내가 참여 중인 일정인지 여부")
        boolean isParticipating,
        @Schema(description = "생성 시간")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @Schema(description = "일정 상태")
        ScheduleStatus status
) {
        public static CrewScheduleListResponse of(
                CrewSchedule schedule,
                Long currentMemberId
        ) {
                return new CrewScheduleListResponse(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getRunType(),
                        schedule.getMeetingAt(),
                        schedule.getLocation().getPlaceName(),
                        schedule.getDistance(),
                        schedule.getPace(),
                        schedule.getMaxParticipants(),
                        schedule.getParticipantIds().size(),
                        schedule.getCreatorId().equals(currentMemberId),
                        schedule.isParticipating(currentMemberId),
                        schedule.getCreatedAt(),
                        schedule.getStatus()
                );
        }
}