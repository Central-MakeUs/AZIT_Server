package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCrewResponse(
        @Schema(description = "초대코드")
        String invitationCode
) {
    public static CreateCrewResponse from(String invitationCode) {
        return new CreateCrewResponse(invitationCode);
    }
}