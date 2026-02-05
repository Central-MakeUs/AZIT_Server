package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long>, CrewMemberRepositoryCustom {
    long countByCrewIdAndStatus(Long crewId, CrewMemberStatus status);
    Optional<CrewMemberEntity> findByCrewIdAndMemberId(Long crewId, Long memberId);
    Optional<CrewMemberEntity> findFirstByMemberIdAndStatusInOrderByIdDesc(Long memberId, Collection<CrewMemberStatus> statuses);

    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 비워 데이터 불일치 방지
    @Query("UPDATE CrewMemberEntity cm SET cm.status = :status WHERE cm.memberId = :memberId")
    void updateAllStatusByMemberId(@Param("memberId") Long memberId, @Param("status") CrewMemberStatus status);

}
