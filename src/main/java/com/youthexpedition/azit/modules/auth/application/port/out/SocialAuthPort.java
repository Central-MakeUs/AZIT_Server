package com.youthexpedition.azit.modules.auth.application.port.out;

import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public interface SocialAuthPort {
    SocialProfile getSocialProfile(SocialLoginCommand command);
    void revoke(SocialRevokeCommand command);

    SocialProvider getProvider(); // 각 소셜 어댑터 식별
}
