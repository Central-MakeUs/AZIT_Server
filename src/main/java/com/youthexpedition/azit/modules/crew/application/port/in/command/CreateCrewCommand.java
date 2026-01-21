package com.youthexpedition.azit.modules.crew.application.port.in.command;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;

public record CreateCrewCommand(
        String name,
        CrewCategory category,
        Region region,
        Long leaderId
) {
    public static CreateCrewCommand of(String name, String category, String region, Long leaderId) {
        return new CreateCrewCommand(name, CrewCategory.of(category), Region.of(region), leaderId);
    }
}
