package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LoadCrewSchedulePort {
    Optional<CrewSchedule> findById(Long scheduleId);
    List<CrewSchedule> findAllByFilter(Long crewId, LocalDate date, RunType runType);
    Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, YearMonth yearMonth);
    List<CrewSchedule> findAllByMemberId(Long memberId);
}
