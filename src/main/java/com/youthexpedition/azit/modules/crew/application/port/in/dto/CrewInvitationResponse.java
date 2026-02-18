package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import io.swagger.v3.oas.annotations.media.Schema;

public record CrewInvitationResponse(
        @Schema(description = "크루 ID")
        Long crewId,
        @Schema(description = "크루 이름")
        String name,
        @Schema(description = "크루 카테고리")
        String category,
        @Schema(description = "크루에 가입되어 있는 멤버 수")
        long memberCount,
        @Schema(description = "크루 이미지 url")
        String crewImageUrl
) {
    public static CrewInvitationResponse of(Crew crew, long memberCount) {
        return new CrewInvitationResponse(
                crew.getId(),
                crew.getName(),
                crew.getCategory().name(),
                memberCount,
                crew.getImageUrl()
        );
    }
}