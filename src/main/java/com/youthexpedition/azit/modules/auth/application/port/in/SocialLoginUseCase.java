package com.youthexpedition.azit.modules.auth.application.port.in;

import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;

public interface SocialLoginUseCase {
    AuthResult login(SocialLoginCommand command);
}
