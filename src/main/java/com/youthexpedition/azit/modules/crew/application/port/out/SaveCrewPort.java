package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.Crew;

public interface SaveCrewPort {
    Crew save(Crew crew);
}
