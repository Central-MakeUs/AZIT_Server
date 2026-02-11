package com.youthexpedition.azit.modules.crew.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CrewMemberRole {
    LEADER("리더"),
    MEMBER("멤버");

    private final String description;
}
