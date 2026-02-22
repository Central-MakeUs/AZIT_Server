package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;

import java.util.List;

public interface SaveCrewSchedulePort {
    void save(CrewSchedule crewSchedule);
    void saveAll(List<CrewSchedule> crewSchedules);
}
