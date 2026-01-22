package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record JoinCrewRequest(
        @Schema(description = "초대코드")
        String invitationCode
) {
    public JoinCrewCommand toCommand(Long memberId) {
        return JoinCrewCommand.of(memberId, invitationCode);
    }
}
