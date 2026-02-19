package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;

import java.util.Optional;

public interface LoadCrewSchedulePort {
    Optional<CrewSchedule> findById(Long scheduleId);
}
