package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberEntity;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public Member toDomain(MemberEntity entity) {
        if (entity == null) return null;

        return Member.builder()
                .id(entity.getId())
                .socialProvider(entity.getSocialProvider())
                .socialProviderId(entity.getSocialProviderId())
                .nickname(entity.getNickname())
                .email(entity.getEmail())
                .isEmailSharingEnabled(entity.isEmailSharingEnabled())
                .profileImageUrl(entity.getProfileImageUrl())
                .appleRefreshToken(entity.getAppleRefreshToken())
                .status(entity.getStatus())
                .role(entity.getRole())
                .totalPoints(entity.getTotalPoints())
                .totalAttendanceCount(entity.getTotalAttendanceCount())
                .essentialTermsAgreedAt(entity.getEssentialTermsAgreedAt())
                .isMarketingTermsAgreed(entity.isMarketingTermsAgreed())
                .marketingTermsAgreedAt(entity.getMarketingTermsAgreedAt())
                .isNotificationAgreed(entity.isNotificationAgreed())
                .notificationAgreedAt(entity.getNotificationAgreedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MemberEntity toEntity(Member domain) {
        return MemberEntity.builder()
                .id(domain.getId())
                .socialProvider(domain.getSocialProvider())
                .socialProviderId(domain.getSocialProviderId())
                .nickname(domain.getNickname())
                .email(domain.getEmail())
                .isEmailSharingEnabled(domain.isEmailSharingEnabled())
                .profileImageUrl(domain.getProfileImageUrl())
                .appleRefreshToken(domain.getAppleRefreshToken())
                .status(domain.getStatus())
                .role(domain.getRole())
                .totalPoints(domain.getTotalPoints())
                .totalAttendanceCount(domain.getTotalAttendanceCount())
                .essentialTermsAgreedAt(domain.getEssentialTermsAgreedAt())
                .isMarketingTermsAgreed(domain.isMarketingTermsAgreed())
                .marketingTermsAgreedAt(domain.getMarketingTermsAgreedAt())
                .isNotificationAgreed(domain.isNotificationAgreed())
                .notificationAgreedAt(domain.getNotificationAgreedAt())
                .build();
    }
}
