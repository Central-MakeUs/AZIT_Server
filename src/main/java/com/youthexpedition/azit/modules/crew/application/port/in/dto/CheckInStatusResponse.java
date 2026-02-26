package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CheckInStatusResponse(
        @Schema(description = "오늘 참여할 일정이 있는지 여부")
        boolean hasScheduleToday,
        @Schema(description = "오늘의 일정 정보")
        TodayScheduleResponse todayScheduleInfo,
        @Schema(description = "다음 일정 정보")
        NextScheduleResponse nextScheduleInfo
) {
    public record TodayScheduleResponse(
            @Schema(description = "일정 ID")
            Long scheduleId,
            @Schema(description = "일정 제목")
            String title,
            @Schema(description = "러닝 타입")
            RunType runType,
            @Schema(description = "집합 장소 위도")
            Double latitude,
            @Schema(description = "집합 장소 경도")
            Double longitude,
            @Schema(description = "출석 완료 여부")
            boolean isCheckedIn,
            @Schema(description = "출석 완료 시간")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime checkedInAt,
            @Schema(description = "출석 가능 시간 여부 (시작 1시간 전~후)")
            boolean isAvailableTime
    ) {
        public static TodayScheduleResponse of(CrewSchedule schedule, boolean isCheckedIn, LocalDateTime checkedInAt, boolean isAvailableTime) {
            return new TodayScheduleResponse(
                    schedule.getId(),
                    schedule.getTitle(),
                    schedule.getRunType(),
                    schedule.getLocation().getLatitude(),
                    schedule.getLocation().getLongitude(),
                    isCheckedIn,
                    checkedInAt,
                    isAvailableTime
            );
        }
    }

    public record NextScheduleResponse(
            @Schema(description = "다음 일정 제목")
            String title,
            @Schema(description = "다음 일정까지 남은 일수")
            long daysLeft
    ) {
        public static NextScheduleResponse of(CrewSchedule schedule, long daysLeft) {
            return new NextScheduleResponse(schedule.getTitle(), daysLeft);
        }
    }

    public static CheckInStatusResponse of(boolean hasScheduleToday, TodayScheduleResponse todayScheduleInfo, NextScheduleResponse nextScheduleInfo) {
        return new CheckInStatusResponse(hasScheduleToday, todayScheduleInfo, nextScheduleInfo);
    }
}