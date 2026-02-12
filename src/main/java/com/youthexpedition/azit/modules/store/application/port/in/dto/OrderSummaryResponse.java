package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderSummaryResponse(
        @Schema(description = "총 상품금액 (할인 전 합계)")
        long totalProductPrice,
        @Schema(description = "아지트 멤버십 할인 금액")
        long membershipDiscount,
        @Schema(description = "포인트 할인 금액")
        long pointDiscount,
        @Schema(description = "배송비")
        long shippingFee,
        @Schema(description = "총 결제 금액")
        long totalPaymentPrice
) {
    public static OrderSummaryResponse of(long totalProductPrice, long membershipDiscount, long pointDiscount, long shippingFee) {
        long totalPaymentPrice = totalProductPrice - membershipDiscount - pointDiscount + shippingFee;
        return new OrderSummaryResponse(
                totalProductPrice,
                membershipDiscount,
                pointDiscount,
                shippingFee,
                totalPaymentPrice
        );
    }
}