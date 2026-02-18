package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;

public interface CrewScheduleUseCase {
    void createSchedule(CreateScheduleCommand command);
}
