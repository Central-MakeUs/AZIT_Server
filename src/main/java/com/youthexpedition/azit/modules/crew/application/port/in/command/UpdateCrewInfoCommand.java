package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record UpdateCrewInfoCommand(
        String name,
        String description
) {
    public static UpdateCrewInfoCommand of(String name, String description) {
        return new UpdateCrewInfoCommand(name, description);
    }
}
