package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record CancelScheduleCommand(
        Long crewId,
        Long scheduleId,
        Long creatorId
) {
    public static CancelScheduleCommand of(Long crewId, Long scheduleId, Long creatorId) {
        return new CancelScheduleCommand(crewId, scheduleId, creatorId);
    }
}
