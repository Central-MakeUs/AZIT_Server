package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewScheduleMemberRepository extends JpaRepository<CrewScheduleMemberEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CrewScheduleMemberEntity csm WHERE csm.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
