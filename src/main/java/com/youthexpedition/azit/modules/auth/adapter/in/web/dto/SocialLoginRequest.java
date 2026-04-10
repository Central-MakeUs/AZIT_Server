package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginRequest (
        @Schema(description = "소셜 서비스로부터 발급받은 인가 코드")
        String authorizationCode,

        @Schema(description = "카카오 네이티브 SDK로부터 발급받은 액세스 토큰 (네이티브 SDK)")
        String accessToken
){
    public SocialLoginCommand toCommand(SocialProvider provider) {
        if (accessToken != null && !accessToken.isBlank()) {
            return SocialLoginCommand.ofKakaoNative(provider, accessToken);
        }
        return SocialLoginCommand.of(provider, authorizationCode);
    }
}
