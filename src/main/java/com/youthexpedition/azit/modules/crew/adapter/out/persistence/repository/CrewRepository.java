package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<CrewEntity, Long> {
}
