package com.youthexpedition.azit.modules.member.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MemberTermsConsent {
    private final Long id;
    private final Long memberId;
    private final Long termsVersionId;
    private final LocalDateTime agreedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberTermsConsent agree(Long memberId, Long termsVersionId) {
        return MemberTermsConsent.builder()
                .memberId(memberId)
                .termsVersionId(termsVersionId)
                .agreedAt(LocalDateTime.now())
                .build();
    }
}
