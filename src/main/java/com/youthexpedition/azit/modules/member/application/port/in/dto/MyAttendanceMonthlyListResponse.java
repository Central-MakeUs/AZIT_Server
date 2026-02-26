package com.youthexpedition.azit.modules.member.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record MyAttendanceMonthlyListResponse(
        @Schema(description = "날짜")
        LocalDate date,

        @Schema(description = "정기런 존재 여부")
        boolean hasRegular,

        @Schema(description = "번개런 존재 여부")
        boolean hasLightning
) {
        public static MyAttendanceMonthlyListResponse of(LocalDate date, boolean hasRegular, boolean hasLightning) {
                return new MyAttendanceMonthlyListResponse(date, hasRegular, hasLightning);
        }
}