package com.youthexpedition.azit.modules.auth.application.port.out;

import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;

public interface SocialAuthPort {
    SocialProfile getSocialProfile(String authorizationCode);
}
