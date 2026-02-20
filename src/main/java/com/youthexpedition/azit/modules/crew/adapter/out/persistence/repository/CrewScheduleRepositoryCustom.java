package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CrewScheduleRepositoryCustom {
    List<CrewScheduleEntity> findAllByFilter(Long crewId, LocalDate date, RunType runType);
    Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, YearMonth yearMonth);
}
