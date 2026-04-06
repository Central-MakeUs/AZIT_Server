package com.youthexpedition.azit.modules.member.application.port.in.command;

public record UpdateNicknameCommand(String nickname) {

    public static UpdateNicknameCommand of(String nickname) {
        return new UpdateNicknameCommand(nickname);
    }
}
