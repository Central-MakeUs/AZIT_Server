package com.youthexpedition.azit.modules.crew.application.port.in.query;

import java.time.LocalDate;
import java.time.YearMonth;

public record CrewScheduleMonthlyQuery(
        Long crewId,
        YearMonth yearMonth,
        LocalDate startDate,
        LocalDate endDate,
        Long memberId
) {
    public static CrewScheduleMonthlyQuery of(Long crewId, LocalDate startDate, LocalDate endDate, YearMonth yearMonth, Long memberId) {
        return new CrewScheduleMonthlyQuery(crewId, yearMonth, startDate, endDate, memberId);
    }
}
