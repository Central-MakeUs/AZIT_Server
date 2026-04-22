package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record UpdateCrewProfileCommand(
        String imageUrl,
        String name,
        String description
) {
    public static UpdateCrewProfileCommand of(String imageUrl, String name, String description) {
        return new UpdateCrewProfileCommand(imageUrl, name, description);
    }
}
