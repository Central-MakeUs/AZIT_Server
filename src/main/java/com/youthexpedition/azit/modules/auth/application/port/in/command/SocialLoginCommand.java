package com.youthexpedition.azit.modules.auth.application.port.in.command;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public record SocialLoginCommand (
        SocialProvider socialProvider,
        String authorizationCode // 인가 코드
) {
    public static SocialLoginCommand of(SocialProvider socialProvider, String authorizationCode) { // 여러 개의 인자를 받아 적절한 객체를 생성하는 메서드
        return new SocialLoginCommand(socialProvider, authorizationCode);
    }
}
