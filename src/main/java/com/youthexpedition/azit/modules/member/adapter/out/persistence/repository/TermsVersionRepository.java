package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.TermsVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TermsVersionRepository extends JpaRepository<TermsVersionEntity, Long> {

    // 각 terms_type별 effective_at이 가장 최신인 버전 1건씩 조회
    @Query("""
            SELECT tv FROM TermsVersionEntity tv
            WHERE tv.effectiveAt = (
                SELECT MAX(tv2.effectiveAt) FROM TermsVersionEntity tv2
                WHERE tv2.termsType = tv.termsType
            )
            """)
    List<TermsVersionEntity> findAllLatest();
}
