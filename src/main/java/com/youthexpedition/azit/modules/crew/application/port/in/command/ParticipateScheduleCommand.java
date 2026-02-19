package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record ParticipateScheduleCommand(
        Long crewId,
        Long scheduleId,
        Long memberId
) {
    public static ParticipateScheduleCommand of(Long scheduleId, Long crewId, Long memberId) {
        return new ParticipateScheduleCommand(scheduleId, crewId, memberId);
    }
}
