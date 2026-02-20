package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.util.List;

public interface CrewScheduleRepositoryCustom {
    List<CrewScheduleEntity> findAllByFilter(Long crewId, LocalDate date, RunType runType);
}
