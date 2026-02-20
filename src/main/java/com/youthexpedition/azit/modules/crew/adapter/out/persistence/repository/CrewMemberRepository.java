package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long>, CrewMemberRepositoryCustom {
    Optional<CrewMemberEntity> findByCrewIdAndMemberId(Long crewId, Long memberId);
    Optional<CrewMemberEntity> findFirstByMemberIdAndStatusInOrderByIdDesc(Long memberId, Collection<CrewMemberStatus> statuses);
    long countByMemberIdAndStatus(Long memberId, CrewMemberStatus status);
    List<CrewMemberEntity> findAllByMemberId(Long memberId);
    List<CrewMemberEntity> findByCrew_IdAndMemberIdIn(Long crewId, List<Long> memberIds);
}
