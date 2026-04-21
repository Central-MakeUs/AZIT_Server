package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CrewInfoResponse(
        @Schema(description = "크루 이미지 URL")
        String crewImageUrl,
        @Schema(description = "크루 이름")
        String name,
        @Schema(description = "크루 한줄 소개")
        String description
) {
    public static CrewInfoResponse of(String crewImageUrl, String name, String description) {
        return new CrewInfoResponse(crewImageUrl, name, description);
    }
}
