package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;

public record JoinCrewRequest(
        String invitationCode
) {
    public JoinCrewCommand toCommand(Long memberId) {
        return JoinCrewCommand.of(memberId, invitationCode);
    }
}
