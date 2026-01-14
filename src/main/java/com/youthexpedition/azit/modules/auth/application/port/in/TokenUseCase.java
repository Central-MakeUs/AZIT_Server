package com.youthexpedition.azit.modules.auth.application.port.in;

import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;

public interface TokenUseCase {
    AuthResult reissue(String refreshToken);
    void logout(Long memberId);
}
