package com.youthexpedition.azit.modules.auth.application.port.in.command;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public record SocialLoginCommand (
        SocialProvider socialProvider,
        String authorizationCode, // 인가 코드
        String idToken,    // 애플 전용
        String user        // 애플 최초 가입 시 사용자 정보 (JSON)
) {
    // 애플용
    public static SocialLoginCommand of(SocialProvider socialProvider, String authorizationCode, String idToken, String user) { // 여러 개의 인자를 받아 적절한 객체를 생성하는 메서드
        return new SocialLoginCommand(socialProvider, authorizationCode, idToken, user);
    }

    // 카카오용
    public static SocialLoginCommand of(SocialProvider socialProvider, String authorizationCode) {
        return new SocialLoginCommand(socialProvider, authorizationCode, null, null);
    }
}
