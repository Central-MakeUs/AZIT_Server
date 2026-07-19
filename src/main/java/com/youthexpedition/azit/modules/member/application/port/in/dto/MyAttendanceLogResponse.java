package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.member.domain.model.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyAttendanceLogResponse(
        @Schema(description = "이번 달 총 출석 횟수")
        int totalAttendanceCount,
        @Schema(description = "이번 달 획득한 누적 포인트")
        long totalPoints,
        @Schema(description = "일자별 활동 상세 리스트")
        List<DailyAttendanceLog> attendanceLogs
) {

    public record DailyAttendanceLog(
            @Schema(description = "일정 ID")
            Long scheduleId,
            @Schema(description = "크루명")
            String crewName,
            @Schema(description = "일정 제목")
            String title,
            @Schema(description = "러닝 타입")
            RunType runType,
            @Schema(description = "모임 시간")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime meetingAt,
            @Schema(description = "집합 장소명")
            String placeName,
            @Schema(description = "출석 상태")
            AttendanceStatus status,
            @Schema(description = "최대 인원")
            Integer maxParticipants,
            @Schema(description = "현재 참여 인원")
            Integer currentParticipants
    ) {
        public static DailyAttendanceLog of(Long scheduleId, String crewName, String title, RunType runType,
                                            LocalDateTime meetingAt, String placeName, AttendanceStatus status,
                                            Integer maxParticipants, Integer currentParticipants) {
            return new DailyAttendanceLog(scheduleId, crewName, title, runType, meetingAt, placeName, status,
                    maxParticipants, currentParticipants);
        }
    }

    public static MyAttendanceLogResponse of(int totalAttendanceCount, long totalPoints, List<DailyAttendanceLog> attendanceLogs) {
        return new MyAttendanceLogResponse(totalAttendanceCount, totalPoints, attendanceLogs);
    }
}