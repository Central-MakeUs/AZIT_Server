package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record ParticipateScheduleCommand(
        Long crewId,
        Long scheduleId,
        Long memberId
) {
    public static ParticipateScheduleCommand of(Long crewId, Long scheduleId, Long memberId) {
        return new ParticipateScheduleCommand(crewId, scheduleId, memberId);
    }
}
