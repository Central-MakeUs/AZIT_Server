package com.youthexpedition.azit.modules.member.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MemberTermsConsentHistory {
    private final Long id;
    private final Long memberId;
    private final Long termsVersionId;
    private final boolean isAgreed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static MemberTermsConsentHistory create(Long memberId, Long termsVersionId, boolean isAgreed) {
        return MemberTermsConsentHistory.builder()
                .memberId(memberId)
                .termsVersionId(termsVersionId)
                .isAgreed(isAgreed)
                .build();
    }
}
