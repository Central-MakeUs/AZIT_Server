package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberTermsConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

public interface MemberTermsConsentRepository extends JpaRepository<MemberTermsConsentEntity, Long> {

    @Query("SELECT c.termsVersionId FROM MemberTermsConsentEntity c WHERE c.memberId = :memberId")
    Set<Long> findTermsVersionIdsByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberTermsConsentEntity c SET c.agreedAt = :agreedAt WHERE c.memberId = :memberId AND c.termsVersionId IN :versionIds")
    void updateAgreedAt(@Param("memberId") Long memberId, @Param("versionIds") Collection<Long> versionIds, @Param("agreedAt") LocalDateTime agreedAt);
}
