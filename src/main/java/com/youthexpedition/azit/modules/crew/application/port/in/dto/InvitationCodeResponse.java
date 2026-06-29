package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InvitationCodeResponse(
        @Schema(description = "새로 발급된 초대 코드")
        String invitationCode
) {
    public static InvitationCodeResponse of(String invitationCode) {
        return new InvitationCodeResponse(invitationCode);
    }
}
