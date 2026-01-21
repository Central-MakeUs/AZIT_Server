package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long> {
    boolean existsByCrewIdAndMemberId(Long crewId, Long memberId);
    long countByCrewIdAndStatus(Long crewId, CrewMemberStatus status);
}
