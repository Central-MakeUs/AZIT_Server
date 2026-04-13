package com.youthexpedition.azit.modules.member.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointHistoryType {
    ATTENDANCE("출석 체크"),
    STORE_USE("스토어 사용"),
    STORE_USE_REFUND("스토어 주문 취소 환불");

    private final String description;
}
