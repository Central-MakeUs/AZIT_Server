package com.youthexpedition.azit.modules.auth.domain.model;

public record AuthToken(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}
