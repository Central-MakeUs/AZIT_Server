package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.TermsVersionEntity;
import com.youthexpedition.azit.modules.member.domain.model.TermsVersion;
import org.springframework.stereotype.Component;

@Component
public class TermsVersionMapper {

    public TermsVersion toDomain(TermsVersionEntity entity) {
        return TermsVersion.builder()
                .id(entity.getId())
                .termsType(entity.getTermsType())
                .version(entity.getVersion())
                .isRequired(entity.isRequired())
                .effectiveAt(entity.getEffectiveAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
