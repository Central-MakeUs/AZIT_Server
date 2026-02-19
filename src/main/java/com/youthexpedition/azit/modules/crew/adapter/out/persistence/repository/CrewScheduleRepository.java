package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CrewScheduleRepository extends JpaRepository<CrewScheduleEntity, Long> {
    @Query("select distinct s from CrewScheduleEntity s " +
            "left join fetch s.supplies " +
            "left join fetch s.members " +
            "where s.id = :scheduleId")
    Optional<CrewScheduleEntity> findByIdWithDetails(@Param("scheduleId") Long scheduleId);
}
