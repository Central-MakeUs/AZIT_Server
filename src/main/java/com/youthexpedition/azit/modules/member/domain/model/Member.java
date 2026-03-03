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

    // 약관 동의 시 멤버 상태 업데이트
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

        this.status = MemberStatus.PENDING_ONBOARDING; // 약관 완료 후 온보딩 대기 상태로 변경
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

    // 리더가 크루 생성 완료했을 경우 상태 변경 (ACTIVE)
    public void completeOnboarding() {
        if (!this.status.isJoinable()) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        this.status = MemberStatus.ACTIVE;
    }

    // 크루원이 초대 코드 입력 후 승인 대기할 경우 상태 변경 (WAITING_FOR_APPROVE)
    public void applyForJoin() {
        if (!this.status.isJoinable()) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        this.status = MemberStatus.WAITING_FOR_APPROVE;
    }

    // 리더가 가입 신청을 승인했을 경우 상태 변경 (ACTIVE)
    public void approveJoin() {
        if (this.status != MemberStatus.WAITING_FOR_APPROVE) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        this.status = MemberStatus.APPROVED_PENDING_CONFIRM;
    }

    // 리더가 가입 신청을 거절했을 경우 상태 변경 (PENDING_ONBOARDING)
    public void rejectJoin() {
        if (this.status != MemberStatus.WAITING_FOR_APPROVE) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        this.status = MemberStatus.REJECTED_PENDING_CONFIRM;
    }

    // 리더가 방출했을 경우 상태 변경 (KICKED_PENDING_CONFIRM)
    public void expel() {
        // 이미 탈퇴한 회원이 아닌 경우에만 상태 변경
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.KICKED_PENDING_CONFIRM;
    }

    // 승인/거절 결과 확인 후 상태 확정
    public void confirmStatus(boolean hasJoinedCrews) {
        this.status = switch (this.status) {
            case APPROVED_PENDING_CONFIRM -> MemberStatus.ACTIVE; // 승인 확인 시 정회원으로 변경
            // 거절 또는 방출 확인 시: 가입된 크루가 하나라도 있으면 ACTIVE, 없으면 온보딩 상태로 변경
            case REJECTED_PENDING_CONFIRM, KICKED_PENDING_CONFIRM ->
                    hasJoinedCrews ? MemberStatus.ACTIVE : MemberStatus.PENDING_ONBOARDING;
            default -> throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS); // 그 외 상태는 예외 처리
        };
    }

    // 탈퇴 상태로 변경
    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN); // 이미 탈퇴한 경우 예외 처리
        }

        this.status = MemberStatus.WITHDRAWN;
        this.appleRefreshToken = null;
    }

    // 탈퇴 상태에서 재로그인 할 경우 ACTIVE 처리
    public void reactivate() {
        if (this.status != MemberStatus.WITHDRAWN) {
            return; // 탈퇴 상태가 아니면 패스
        }

        this.status = MemberStatus.PENDING_ONBOARDING; // 추후 기획 확인 필요
    }

    // 가입된 모든 크루에서 탈퇴하거나 방출되었을 경우, 앱 사용 제한을 위해 다시 크루 가입 단계로 되돌림
    public void resetToOnboarding() {
        // 탈퇴한 회원이 아닌 경우에만 상태 변경
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.PENDING_ONBOARDING;
    }

    // 포인트가 충분한지 체크
    public boolean hasEnoughPoints(long points) {
        return this.totalPoints >= points;
    }

    // 포인트 차감
    public void deductPoints(long points) {
        if (!hasEnoughPoints(points)) {
            throw new BusinessException(MemberErrorCode.INSUFFICIENT_POINTS);
        }
        this.totalPoints -= points;
    }

    // 포인트 적립
    public void addPoints(long points) {
        if (points < 0) {
            throw new BusinessException(MemberErrorCode.INVALID_POINT_VALUE);
        }

        this.totalPoints += points;
    }

    public void updateAttendanceCount() {
        this.totalAttendanceCount++;
    }
}
