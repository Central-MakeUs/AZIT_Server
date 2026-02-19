package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateScheduleCommand;

public interface CrewScheduleUseCase {
    void createSchedule(CreateScheduleCommand command);
    void updateSchedule(UpdateScheduleCommand command);
}
