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
                .status(entity.getStatus())
                .role(entity.getRole())
                .totalPoints(entity.getTotalPoints())
                .build();
    }

    public MemberEntity toEntity(Member member) {
        if (member == null) return null;

        return MemberEntity.builder()
                .socialProvider(member.getSocialProvider())
                .socialProviderId(member.getSocialProviderId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .status(member.getStatus())
                .role(member.getRole())
                .totalPoints(member.getTotalPoints())
                .build();
    }
}
