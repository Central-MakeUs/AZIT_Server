package com.youthexpedition.azit.modules.auth.application.port.in;

import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;

public interface TokenUseCase {
    AuthToken reissue(String refreshToken);
    void logout(Long memberId);
}
