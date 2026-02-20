package com.youthexpedition.azit.modules.crew.application.port.in.query;

import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;

public record CrewScheduleQuery(
        Long crewId,
        LocalDate date,
        RunType runType,
        Long memberId
) {
    public static CrewScheduleQuery of(Long crewId, LocalDate date, RunType runType, Long memberId) {
        return new CrewScheduleQuery(crewId, date, runType, memberId);
    }
}
