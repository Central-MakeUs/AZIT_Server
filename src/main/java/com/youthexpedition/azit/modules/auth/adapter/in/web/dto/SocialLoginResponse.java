package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SocialLoginResponse(
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "액세스 토큰 만료 시간 (초)")
        long accessTokenExpiresIn // 액세스 토큰 만료 시간
) {
    public static SocialLoginResponse from(AuthToken token) { // from: 타입 하나를 받아서 새로운 객체 생성하는 메서드(계층 간 데이터 변환)
        return SocialLoginResponse.builder()
                .accessToken(token.accessToken())
                .accessTokenExpiresIn(token.accessTokenExpiresIn())
                .build();
    }
}
