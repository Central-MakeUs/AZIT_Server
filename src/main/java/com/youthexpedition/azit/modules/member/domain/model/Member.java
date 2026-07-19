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
    private LocalDateTime withdrawnAt; // 탈퇴 시점 (유예기간 만료 후 배치 파기 기준)
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static final long WITHDRAWAL_GRACE_PERIOD_DAYS = 30L; // 탈퇴 유예기간 (이내 재로그인 시 복구 가능)

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

    // 탈퇴(유예기간 중) 또는 파기 완료 상태인지 확인
    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN || this.status == MemberStatus.DELETED;
    }

    public void validateNotWithdrawn() {
        if (isWithdrawn()) throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    // 탈퇴 상태로 변경
    public void withdraw(LocalDateTime withdrawnAt) {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = withdrawnAt;
    }

    // 탈퇴 유예기간 내 재로그인 할 경우 ACTIVE 처리
    public void reactivate(LocalDateTime now) {
        if (this.status == MemberStatus.DELETED) {
            throw new BusinessException(MemberErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED); // 파기 완료된 계정은 복구 불가
        }
        if (this.status != MemberStatus.WITHDRAWN) {
            return; // 탈퇴 상태가 아니면 패스
        }
        if (isGracePeriodExpired(now)) {
            throw new BusinessException(MemberErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
        }
        this.status = MemberStatus.ACTIVE;
        this.withdrawnAt = null;
    }

    // 탈퇴 유예기간 만료 여부
    public boolean isGracePeriodExpired(LocalDateTime now) {
        return this.withdrawnAt != null && this.withdrawnAt.plusDays(WITHDRAWAL_GRACE_PERIOD_DAYS).isBefore(now);
    }

    // 포인트가 충분한지 체크
    public boolean hasEnoughPoints(long points) {
        return this.totalPoints >= points;
    }

    public void validateEnoughPoints(long points) {
        if (!hasEnoughPoints(points)) throw new BusinessException(MemberErrorCode.INSUFFICIENT_POINTS);
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
