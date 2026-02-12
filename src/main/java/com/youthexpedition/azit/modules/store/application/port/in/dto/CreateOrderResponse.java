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
        OrderSummaryResponse summary
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

        public static CreateOrderResponse of(String orderNumber, OrderDeliveryInfoResponse deliveryInfo,
                                             DepositAccountInfoResponse depositAccountInfo, OrderSummaryResponse summary) {
                return new CreateOrderResponse(orderNumber, deliveryInfo, depositAccountInfo, summary);
        }
}