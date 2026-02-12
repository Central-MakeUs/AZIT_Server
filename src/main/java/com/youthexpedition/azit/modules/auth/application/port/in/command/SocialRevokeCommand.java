package com.youthexpedition.azit.modules.auth.application.port.in.command;

import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public record SocialRevokeCommand(
        SocialProvider provider,
        String socialProviderId,
        String refreshToken // 애플용
) {
    public static SocialRevokeCommand from(Member member) {
        return new SocialRevokeCommand(
                member.getSocialProvider(),
                member.getSocialProviderId(),
                member.getAppleRefreshToken()
        );
    }
}
