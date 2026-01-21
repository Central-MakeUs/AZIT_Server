package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record JoinCrewCommand(
        Long memberId,
        String invitationCode
) {
    public static JoinCrewCommand of(Long memberId, String invitationCode) {
        return new JoinCrewCommand(memberId, invitationCode);
    }
}
