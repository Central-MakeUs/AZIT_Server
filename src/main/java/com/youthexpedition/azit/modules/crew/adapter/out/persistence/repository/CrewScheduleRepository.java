package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewScheduleRepository extends JpaRepository<CrewScheduleEntity, Long> {
}
