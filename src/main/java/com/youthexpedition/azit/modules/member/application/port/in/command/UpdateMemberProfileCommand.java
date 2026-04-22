package com.youthexpedition.azit.modules.member.application.port.in.command;

public record UpdateMemberProfileCommand(
        String nickname,
        String imageUrl
) {
    public static UpdateMemberProfileCommand of(String nickname, String imageUrl) {
        return new UpdateMemberProfileCommand(nickname, imageUrl);
    }
}
