package com.youthexpedition.azit.modules.crew.application.port.in.query;

import java.time.YearMonth;

public record CrewScheduleMonthlyQuery(
        Long crewId,
        YearMonth yearMonth,
        Long memberId
) {
    public static CrewScheduleMonthlyQuery of(Long crewId, YearMonth yearMonth, Long memberId) {
        return new CrewScheduleMonthlyQuery(
                crewId,
                yearMonth != null ? yearMonth : YearMonth.now(),
                memberId
        );
    }
}
