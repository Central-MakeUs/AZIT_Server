package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;

public interface SaveCrewSchedulePort {
    void save(CrewSchedule crewSchedule);
}
