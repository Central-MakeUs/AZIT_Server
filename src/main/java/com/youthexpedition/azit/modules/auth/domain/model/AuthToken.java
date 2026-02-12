package com.youthexpedition.azit.modules.auth.domain.model;

import lombok.Builder;

@Builder
public record AuthToken(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}
