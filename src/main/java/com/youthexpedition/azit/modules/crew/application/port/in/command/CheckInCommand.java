package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record CheckInCommand(
        Long memberId,
        Long scheduleId,
        Double latitude,
        Double longitude
) {
    public static CheckInCommand of(Long memberId, Long scheduleId, Double latitude, Double longitude) {
        return new CheckInCommand(memberId, scheduleId, latitude, longitude);
    }
}
