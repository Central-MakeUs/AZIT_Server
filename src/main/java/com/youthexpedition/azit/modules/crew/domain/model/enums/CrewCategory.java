package com.youthexpedition.azit.modules.crew.domain.model.enums;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CrewCategory {
    RUNNING("러닝");

    private final String description;

    public static CrewCategory of(String value) {
        return Arrays.stream(CrewCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CrewErrorCode.INVALID_CREW_CATEGORY));
    }
}
