package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoadCrewSchedulePort {
    Optional<CrewSchedule> findById(Long scheduleId);
    List<CrewSchedule> findAllByFilter(Long crewId, LocalDate date, RunType runType);
}
