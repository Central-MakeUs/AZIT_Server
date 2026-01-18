package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Member {
    private final Long id;
    private final SocialProvider socialProvider;
    private final String socialProviderId;
    private String nickname;
    private String email;
    private String profileImageUrl;
    // 애플 리프레시 토큰
    private String appleRefreshToken;
    private MemberStatus status;
    private MemberRole role;
    private Long totalPoints;
    private LocalDateTime essentialTermsAgreedAt; // 필수 약관(전체) 동의 시점
    private boolean isMarketingTermsAgreed; // 마케팅 동의 여부
    private LocalDateTime marketingTermsAgreedAt; // 마케팅 동의 시점
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Member create(SocialProvider provider, String socialProviderId,
                                String nickname, String email, String profileImageUrl) {
        return Member.builder()
                .socialProvider(provider)
                .socialProviderId(socialProviderId)
                .nickname(nickname)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .status(MemberStatus.PENDING_TERMS)
                .role(MemberRole.MEMBER)
                .totalPoints(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 약관 동의 시 멤버 상태 업데이트
    public void completeTermsAgreement(boolean marketingAgreed) {
        if (this.status != MemberStatus.PENDING_TERMS) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }

        this.essentialTermsAgreedAt = LocalDateTime.now();
        this.isMarketingTermsAgreed = marketingAgreed;
        if (marketingAgreed) {
            this.marketingTermsAgreedAt = LocalDateTime.now();
        }

        this.status = MemberStatus.PENDING_ONBOARDING; // 약관 완료 후 온보딩 대기 상태로 변경
        this.updatedAt = LocalDateTime.now();
    }

    // 애플 리프레시 토큰 업데이트
    public void updateAppleRefreshToken(String appleRefreshToken) {
        if (this.socialProvider == SocialProvider.APPLE) {
            this.appleRefreshToken = appleRefreshToken;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // 탈퇴 상태로 변경
    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN); // 이미 탈퇴한 경우 예외 처리
        }

        this.status = MemberStatus.WITHDRAWN;
        this.appleRefreshToken = null;
        this.updatedAt = LocalDateTime.now();
    }
}
