package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.PointHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistoryEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PointHistoryEntity ph WHERE ph.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
