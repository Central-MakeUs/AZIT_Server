package com.youthexpedition.azit.modules.member.adapter.out.persistence;

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
                .profileImageUrl(entity.getProfileImageUrl())
                .appleRefreshToken(entity.getAppleRefreshToken())
                .status(entity.getStatus())
                .role(entity.getRole())
                .totalPoints(entity.getTotalPoints())
                .essentialTermsAgreedAt(entity.getEssentialTermsAgreedAt())
                .isMarketingTermsAgreed(entity.isMarketingTermsAgreed())
                .marketingTermsAgreedAt(entity.getMarketingTermsAgreedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MemberEntity toEntity(Member member) {
        return MemberEntity.builder()
                .id(member.getId())
                .socialProvider(member.getSocialProvider())
                .socialProviderId(member.getSocialProviderId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .appleRefreshToken(member.getAppleRefreshToken())
                .status(member.getStatus())
                .role(member.getRole())
                .totalPoints(member.getTotalPoints())
                .essentialTermsAgreedAt(member.getEssentialTermsAgreedAt())
                .isMarketingTermsAgreed(member.isMarketingTermsAgreed())
                .marketingTermsAgreedAt(member.getMarketingTermsAgreedAt())
                .build();
    }
}
