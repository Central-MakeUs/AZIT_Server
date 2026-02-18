package com.youthexpedition.azit.modules.crew.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmPm {
    AM("오전"),
    PM("오후");

    private final String description;

    // 12시간제 시간을 24시간제로 변환
    public int to24Hour(int hour) {
        if (this == PM) {
            return (hour == 12) ? 12 : hour + 12;
        }
        // AM인 경우
        return (hour == 12) ? 0 : hour;
    }
}
