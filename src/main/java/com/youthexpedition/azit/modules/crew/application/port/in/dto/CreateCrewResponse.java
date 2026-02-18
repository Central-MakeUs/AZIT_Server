package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCrewResponse(
        @Schema(description = "초대코드")
        String invitationCode,
        @Schema(description = "크루 이미지 url")
        String crewImageUrl
) {
    public static CreateCrewResponse of(String invitationCode, String crewImageUrl) {
        return new CreateCrewResponse(invitationCode, crewImageUrl);
    }
}