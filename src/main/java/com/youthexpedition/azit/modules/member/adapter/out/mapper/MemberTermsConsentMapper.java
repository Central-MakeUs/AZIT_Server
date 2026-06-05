package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberTermsConsentEntity;
import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsent;
import org.springframework.stereotype.Component;

@Component
public class MemberTermsConsentMapper {

    public MemberTermsConsent toDomain(MemberTermsConsentEntity entity) {
        return MemberTermsConsent.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .termsVersionId(entity.getTermsVersionId())
                .agreedAt(entity.getAgreedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MemberTermsConsentEntity toEntity(MemberTermsConsent domain) {
        return MemberTermsConsentEntity.builder()
                .memberId(domain.getMemberId())
                .termsVersionId(domain.getTermsVersionId())
                .agreedAt(domain.getAgreedAt())
                .build();
    }
}
