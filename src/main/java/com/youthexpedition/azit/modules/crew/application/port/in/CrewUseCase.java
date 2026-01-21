package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;

public interface CrewUseCase {
    CreateCrewResponse createCrew(CreateCrewCommand command);
}
