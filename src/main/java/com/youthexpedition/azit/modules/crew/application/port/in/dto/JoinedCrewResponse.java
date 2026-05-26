package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record JoinedCrewResponse(
        @Schema(description = "크루 ID")
        Long crewId,
        @Schema(description = "크루명")
        String name,
        @Schema(description = "크루 이미지 URL")
        String imageUrl,
        @Schema(description = "크루 한줄 소개")
        String description
) {}
