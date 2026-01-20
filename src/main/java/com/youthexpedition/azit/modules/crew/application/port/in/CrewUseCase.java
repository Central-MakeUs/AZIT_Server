package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;

public interface CrewUseCase {
    String createCrew(CreateCrewCommand command);
}
