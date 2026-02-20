package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CrewScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;

public interface CrewScheduleUseCase {
    void createSchedule(CreateScheduleCommand command);
    void updateSchedule(UpdateScheduleCommand command);
    void cancelSchedule(CancelScheduleCommand command);
    void participateSchedule(CrewScheduleCommand command);
    void cancelParticipation(CrewScheduleCommand command);
    CrewScheduleDetailResponse getScheduleDetail(CrewScheduleCommand command);
}
