package com.youthexpedition.azit.modules.crew.application.port.in.query;

import java.time.LocalDate;
import java.time.YearMonth;

public record CrewScheduleMonthlyQuery(
        Long crewId,
        LocalDate startDate,
        LocalDate endDate,
        YearMonth yearMonth,
        Long memberId
) {
    public static CrewScheduleMonthlyQuery of(Long crewId, LocalDate startDate, LocalDate endDate, YearMonth yearMonth, Long memberId) {
        return new CrewScheduleMonthlyQuery(crewId, startDate, endDate, yearMonth, memberId);
    }
}
