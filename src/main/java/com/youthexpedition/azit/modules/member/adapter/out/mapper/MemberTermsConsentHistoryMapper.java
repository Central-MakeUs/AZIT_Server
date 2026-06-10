package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberTermsConsentHistoryEntity;
import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsentHistory;
import org.springframework.stereotype.Component;

@Component
public class MemberTermsConsentHistoryMapper {

    public MemberTermsConsentHistoryEntity toEntity(MemberTermsConsentHistory domain) {
        return MemberTermsConsentHistoryEntity.builder()
                .memberId(domain.getMemberId())
                .termsVersionId(domain.getTermsVersionId())
                .isAgreed(domain.isAgreed())
                .build();
    }
}
