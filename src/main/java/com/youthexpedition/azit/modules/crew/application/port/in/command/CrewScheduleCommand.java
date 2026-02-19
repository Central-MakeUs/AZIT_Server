package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record CrewScheduleCommand(
        Long crewId,
        Long scheduleId,
        Long memberId
) {
    public static CrewScheduleCommand of(Long crewId, Long scheduleId, Long memberId) {
        return new CrewScheduleCommand(crewId, scheduleId, memberId);
    }
}
