package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberTermsConsentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberTermsConsentHistoryRepository extends JpaRepository<MemberTermsConsentHistoryEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MemberTermsConsentHistoryEntity h WHERE h.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
