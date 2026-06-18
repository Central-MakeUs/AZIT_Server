package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.Crew;

import java.util.List;

public interface SaveCrewPort {
    Crew save(Crew crew);
    void saveAll(List<Crew> crews);
    void incrementMemberCount(Long crewId);
}
