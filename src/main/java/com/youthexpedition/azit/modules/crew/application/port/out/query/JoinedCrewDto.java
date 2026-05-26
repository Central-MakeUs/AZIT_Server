package com.youthexpedition.azit.modules.crew.application.port.out.query;

public record JoinedCrewDto(
        Long crewId,
        String name,
        String imageUrl,
        String description
) {}
