package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateOrderResponse(
        @Schema(description = "주문 번호")
        String orderNumber,
        @Schema(description = "배송지 정보")
        OrderDeliveryInfoResponse deliveryInfo,
        @Schema(description = "입금 계좌 정보")
        DepositAccountInfoResponse depositAccountInfo,
        @Schema(description = "최종 결제 금액 요약")
        CheckoutSummaryResponse summary
) {
        public record OrderDeliveryInfoResponse(
                @Schema(description = "수령인 이름")
                String recipientName,
                @Schema(description = "수령인 연락처")
                String phoneNumber,
                @Schema(description = "기본 주소")
                String baseAddress,
                @Schema(description = "상세 주소")
                String detailAddress
        ) {
                public static OrderDeliveryInfoResponse of(String recipientName, String phoneNumber, String baseAddress, String detailAddress) {
                        return new OrderDeliveryInfoResponse(recipientName, phoneNumber, baseAddress, detailAddress);
                }
        }

        public record CheckoutSummaryResponse(
                @Schema(description = "총 상품금액 (할인 전 합계)")
                long totalProductPrice,
                @Schema(description = "아지트 멤버십 할인 금액")
                long membershipDiscount,
                @Schema(description = "포인트 할인 금액")
                long pointDiscount,
                @Schema(description = "배송비")
                long shippingFee,
                @Schema(description = "최종 결제 금액")
                long totalPaymentPrice
        ) {
                public static CheckoutSummaryResponse of(long totalProductPrice, long membershipDiscount, long pointDiscount, long shippingFee) {
                        return new CheckoutSummaryResponse(
                                totalProductPrice,
                                membershipDiscount,
                                pointDiscount,
                                shippingFee,
                                totalProductPrice - membershipDiscount - pointDiscount + shippingFee
                        );
                }
        }

        public static CreateOrderResponse of(String orderNumber, OrderDeliveryInfoResponse deliveryInfo,
                                             DepositAccountInfoResponse depositAccountInfo, CheckoutSummaryResponse summary) {
                return new CreateOrderResponse(orderNumber, deliveryInfo, depositAccountInfo, summary);
        }
}