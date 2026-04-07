package com.youthexpedition.azit.modules.member.application.port.in.command;

public record UpdateProfileImageCommand(String imageUrl) {
    public static UpdateProfileImageCommand of(String imageUrl) {
        return new UpdateProfileImageCommand(imageUrl);
    }
}
