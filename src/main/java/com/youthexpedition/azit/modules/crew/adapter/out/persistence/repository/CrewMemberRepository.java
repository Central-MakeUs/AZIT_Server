package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long> {
}
