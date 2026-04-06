package com.youthexpedition.azit.modules.member.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class MemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Column(name = "social_provider_id", nullable = false, length = 255)
    private String socialProviderId;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "email", length = 255)
    private String email;

    @Builder.Default
    @Column(name = "is_email_sharing_enabled", nullable = false)
    private boolean isEmailSharingEnabled = true;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "apple_refresh_token", length = 500)
    private String appleRefreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MemberStatus status;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role = MemberRole.MEMBER;

    @Builder.Default
    @Column(name = "total_points", nullable = false)
    private Long totalPoints = 0L;

    @Builder.Default
    @Column(name = "total_attendance_count", nullable = false)
    private Integer totalAttendanceCount = 0;

    @Column(name = "essential_terms_agreed_at")
    private LocalDateTime essentialTermsAgreedAt; // 필수 약관(전체) 동의 시점

    @Column(name = "is_marketing_terms_agreed", nullable = false)
    private boolean isMarketingTermsAgreed; // 마케팅 동의 여부

    @Column(name = "marketing_terms_agreed_at")
    private LocalDateTime marketingTermsAgreedAt; // 마케팅 동의 시점

    @Column(name = "is_notification_agreed", nullable = false)
    private boolean isNotificationAgreed; // 알림 수신 동의 여부

    @Column(name = "notification_agreed_at")
    private LocalDateTime notificationAgreedAt; // 알림 수신 동의 시점
}
