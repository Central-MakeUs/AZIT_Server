package com.youthexpedition.azit.modules.member.application.port.query;

import java.time.YearMonth;

public record MyAttendanceMonthlyQuery(
        YearMonth yearMonth,
        Long memberId
) {
    public static MyAttendanceMonthlyQuery of(YearMonth yearMonth, Long memberId) {
        return new MyAttendanceMonthlyQuery(
                yearMonth != null ? yearMonth : YearMonth.now(),
                memberId
        );
    }
}
