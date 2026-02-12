package com.youthexpedition.azit.modules.member.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialProvider {
    KAKAO( "카카오"),
    APPLE("애플");

    private final String description;
}
