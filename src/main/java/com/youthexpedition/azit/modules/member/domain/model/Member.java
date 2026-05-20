package com.youthexpedition.azit.modules.member.domain.model;

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
    private boolean isEmailSharingEnabled;
    private String profileImageUrl;
    private String appleRefreshToken; // 애플 리프레시 토큰
    private MemberStatus status;
    private MemberRole role;
    private Long totalPoints;
    private Integer totalAttendanceCount;
    private LocalDateTime essentialTermsAgreedAt; // 필수 약관(전체) 동의 시점
    private boolean isMarketingTermsAgreed; // 마케팅 동의 여부
    private LocalDateTime marketingTermsAgreedAt; // 마케팅 동의 시점
    private boolean isNotificationAgreed; // 알림 동의 여부
    private LocalDateTime notificationAgreedAt; // 알림 동의 시점
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Member create(SocialProvider provider, String socialProviderId,
                                String nickname, String email, boolean isEmailSharingEnabled, String profileImageUrl) {
        return Member.builder()
                .socialProvider(provider)
                .socialProviderId(socialProviderId)
                .nickname(nickname)
                .email(email)
                .isEmailSharingEnabled(isEmailSharingEnabled)
                .profileImageUrl(profileImageUrl)
                .status(MemberStatus.PENDING_TERMS)
                .role(MemberRole.MEMBER)
                .totalPoints(0L)
                .totalAttendanceCount(0)
                .build();
    }

    // 약관 동의 완료 시 ACTIVE로 전환
    public void completeTermsAgreement(boolean marketingAgreed, boolean notificationAgreed) {
        this.essentialTermsAgreedAt = LocalDateTime.now();
        this.isMarketingTermsAgreed = marketingAgreed;
        if (marketingAgreed) {
            this.marketingTermsAgreedAt = LocalDateTime.now();
        }

        this.isNotificationAgreed = notificationAgreed;
        if (notificationAgreed) {
            this.notificationAgreedAt = LocalDateTime.now();
        }

        this.status = MemberStatus.ACTIVE;
    }

    // 애플 리프레시 토큰 업데이트
    public void updateAppleRefreshToken(String appleRefreshToken) {
        if (this.socialProvider == SocialProvider.APPLE) {
            this.appleRefreshToken = appleRefreshToken;
        }
    }

    public void updateEmailSharingStatus(boolean isEnabled) {
        this.isEmailSharingEnabled = isEnabled;
    }

    public boolean isJoinable() {
        return this.status.isJoinable();
    }

    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN;
    }

    // 탈퇴 상태로 변경
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.appleRefreshToken = null;
    }

    // 탈퇴 상태에서 재로그인 할 경우 ACTIVE 처리
    public void reactivate() {
        if (this.status != MemberStatus.WITHDRAWN) {
            return; // 탈퇴 상태가 아니면 패스
        }
        this.status = MemberStatus.ACTIVE;
    }

    // 포인트가 충분한지 체크
    public boolean hasEnoughPoints(long points) {
        return this.totalPoints >= points;
    }

    // 포인트 차감
    public void deductPoints(long points) {
        this.totalPoints -= points;
    }

    // 포인트 적립
    public void addPoints(long points) {
        this.totalPoints += points;
    }

    // 닉네임 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 프로필 이미지 수정
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateAttendanceCount() {
        this.totalAttendanceCount++;
    }
}
