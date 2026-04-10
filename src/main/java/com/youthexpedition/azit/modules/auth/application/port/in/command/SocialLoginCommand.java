package com.youthexpedition.azit.modules.auth.application.port.in.command;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public record SocialLoginCommand (
        SocialProvider socialProvider,
        String authorizationCode, // 인가 코드
        String accessToken,       // 카카오 네이티브 SDK
        String idToken,           // 애플 전용
        String user               // 애플 최초 가입 시 사용자 정보 (JSON)
) {
    // 애플용
    public static SocialLoginCommand of(SocialProvider socialProvider, String authorizationCode, String idToken, String user) {
        return new SocialLoginCommand(socialProvider, authorizationCode, null, idToken, user);
    }

    // 카카오 웹 OAuth용
    public static SocialLoginCommand of(SocialProvider socialProvider, String authorizationCode) {
        return new SocialLoginCommand(socialProvider, authorizationCode, null, null, null);
    }

    // 카카오 네이티브 SDK용
    public static SocialLoginCommand ofKakaoNative(SocialProvider socialProvider, String accessToken) {
        return new SocialLoginCommand(socialProvider, null, accessToken, null, null);
    }
}
