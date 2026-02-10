package com.youthexpedition.azit.modules.store.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "주문 날짜")
        LocalDateTime orderDate,
        @Schema(description = "주문 번호")
        String orderNumber,
        @Schema(description = "배송지 정보")
        DeliveryAddressResponse deliveryInfo,
        @Schema(description = "배송 정보")
        ShippingResponse shippingInfo,
        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items,
        @Schema(description = "결제 금액 요약")
        PaymentSummaryResponse summary
) {
        public record DeliveryAddressResponse(
                @Schema(description = "수령인 이름")
                String recipientName,
                @Schema(description = "수령인 연락처")
                String phoneNumber,
                @Schema(description = "기본 주소")
                String baseAddress,
                @Schema(description = "상세 주소")
                String detailAddress,
                @Schema(description = "배송 요청사항")
                String shippingInstruction
        ) {
                public static DeliveryAddressResponse of(
                        String recipientName, String phoneNumber, String baseAddress, String detailAddress, String shippingInstruction) {
                        return new DeliveryAddressResponse(
                                recipientName,
                                phoneNumber,
                                baseAddress,
                                detailAddress,
                                shippingInstruction
                        );
                }
        }

        public record ShippingResponse(
                @Schema(description = "택배사 (배송 전일 경우 null)")
                String courier,
                @Schema(description = "운송장 번호 (배송 전일 경우 null)")
                String trackingNumber
        ) {}

        public record OrderItemResponse(
                @Schema(description = "구매 당시 브랜드명")
                String brandName,
                @Schema(description = "구매 당시 상품명")
                String productName,
                @Schema(description = "구매 당시 옵션 정보")
                String optionDescription,
                @Schema(description = "구매 당시 상품 대표 이미지 url")
                String productImageUrl,
                @Schema(description = "총 판매가")
                long totalSalePrice,
                @Schema(description = "구매 수량")
                int quantity
        ) {
                public static OrderItemResponse of(String brandName, String productName, String optionDescription,
                                                   String productImageUrl, long totalSalePrice, int quantity) {
                        return new OrderItemResponse(
                                brandName,
                                productName,
                                optionDescription,
                                productImageUrl,
                                totalSalePrice,
                                quantity);
                }
        }

        public record PaymentSummaryResponse(
                @Schema(description = "총 상품금액 (할인 전 합계)")
                long totalProductPrice,
                @Schema(description = "아지트 멤버십 할인 금액")
                long membershipDiscount,
                @Schema(description = "포인트 할인 금액")
                long pointDiscount,
                @Schema(description = "배송비")
                long shippingFee,
                @Schema(description = "최종 결제 예정 금액")
                long totalPaymentPrice
        ) {
                public static PaymentSummaryResponse of(long totalProductPrice, long membershipDiscount, long pointDiscount, long shippingFee) {
                        long totalPaymentPrice = totalProductPrice - membershipDiscount - pointDiscount + shippingFee;
                        return new PaymentSummaryResponse(
                                totalProductPrice,
                                membershipDiscount,
                                pointDiscount,
                                shippingFee,
                                totalPaymentPrice
                        );
                }
        }

        public static OrderDetailResponse of(LocalDateTime orderDate, String orderNumber, DeliveryAddressResponse deliveryInfo,
                                             ShippingResponse shippingInfo, List<OrderItemResponse> items, PaymentSummaryResponse summary) {
                return new OrderDetailResponse(
                        orderDate,
                        orderNumber,
                        deliveryInfo,
                        shippingInfo,
                        items,
                        summary
                );
        }
}