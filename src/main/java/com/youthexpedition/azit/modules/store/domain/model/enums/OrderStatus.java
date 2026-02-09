package com.youthexpedition.azit.modules.store.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("PENDING", "결제 대기"),
    PAID("PAID", "결제 완료"),
    PREPARING("PREPARING", "배송 준비 중"),
    SHIPPING("SHIPPING", "배송 중"),
    DELIVERED("DELIVERED", "배송 완료"),
    CANCELLED("CANCELLED", "주문 취소"),
    EXPIRED("EXPIRED", "입금 기한 만료"),
    REFUNDED("REFUNDED", "환불 완료");

    private final String code;
    private final String description;
}
