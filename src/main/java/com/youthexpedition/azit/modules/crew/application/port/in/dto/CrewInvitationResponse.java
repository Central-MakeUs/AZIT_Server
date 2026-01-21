package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.Crew;

public record CrewInvitationResponse(
        Long crewId,
        String name,
        String category,
        long memberCount
) {
    public static CrewInvitationResponse of(Crew crew, long memberCount) {
        return new CrewInvitationResponse(
                crew.getId(),
                crew.getName(),
                crew.getCategory().name(),
                memberCount
        );
    }
}