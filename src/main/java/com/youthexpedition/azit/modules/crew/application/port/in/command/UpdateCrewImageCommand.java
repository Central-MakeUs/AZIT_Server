package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record UpdateCrewImageCommand(
        String imageUrl
) {
    public static UpdateCrewImageCommand of(String imageUrl) {
        return new UpdateCrewImageCommand(imageUrl);
    }
}
