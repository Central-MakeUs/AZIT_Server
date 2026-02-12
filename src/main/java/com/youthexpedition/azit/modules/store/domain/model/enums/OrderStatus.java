package com.youthexpedition.azit.modules.store.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("결제 대기"),
    PAID("결제 완료"),
    PREPARING("배송 준비 중"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    PURCHASE_CONFIRMED("구매 확정"),
    CANCELLED("주문 취소"),
    EXPIRED("입금 기한 만료"),
    PENDING_REFUNDED("환불 대기"),
    REFUNDED("환불 완료");

    private final String description;
}
