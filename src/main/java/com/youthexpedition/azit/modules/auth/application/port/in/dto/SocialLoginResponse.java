package com.youthexpedition.azit.modules.auth.application.port.in.dto;

import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SocialLoginResponse(
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "액세스 토큰 만료 시간 (초)")
        long accessTokenExpiresIn, // 액세스 토큰 만료 시간
        @Schema(description = "회원 상태")
        MemberStatus status
) {
    public static SocialLoginResponse from(AuthResult authResult) {
        return SocialLoginResponse.builder()
                .accessToken(authResult.authToken().accessToken())
                .accessTokenExpiresIn(authResult.authToken().accessTokenExpiresIn())
                .status(authResult.status())
                .build();
    }
}
