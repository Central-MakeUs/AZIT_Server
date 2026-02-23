package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record CrewJoinStatusResponse(
        @Schema(description = "크루 ID")
        Long crewId,
        @Schema(description = "크루 이름")
        String name,
        @Schema(description = "크루 이미지 url")
        String crewImageUrl,
        @Schema(description = "멤버 상태")
        CrewMemberStatus status
) {
    public static CrewJoinStatusResponse of(Long crewId, String crewName, String crewImageUrl, CrewMemberStatus status) {
        return new CrewJoinStatusResponse(crewId, crewName, crewImageUrl, status);
    }
}
