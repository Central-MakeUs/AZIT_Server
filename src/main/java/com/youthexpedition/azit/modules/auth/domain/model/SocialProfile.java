package com.youthexpedition.azit.modules.auth.domain.model;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.Builder;

@Builder
public record SocialProfile(
        String socialProviderId,
        SocialProvider socialProvider, // KAKAO, APPLE
        String nickname,
        String email,
        String profileImageUrl,
        String refreshToken, // 애플용 리프레시 토큰
        boolean isEmailSharingEnabled // 이메일 공유 여부
) {}
