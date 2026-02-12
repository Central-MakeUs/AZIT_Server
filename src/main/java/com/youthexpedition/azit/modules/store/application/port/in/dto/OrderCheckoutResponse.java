package com.youthexpedition.azit.modules.store.application.port.in.dto;

import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OrderCheckoutResponse(
        @Schema(description = "배송지 정보 (기본 배송지 우선, 없으면 null)")
        DeliveryAddressResponse deliveryInfo,
        @Schema(description = "주문할 상품 목록")
        List<CheckoutItemDetailResponse> items,
        @Schema(description = "포인트 정보")
        PointInfoResponse pointInfo,
        @Schema(description = "사용 가능한 결제 수단 목록")
        List<PaymentMethodResponse> paymentMethods,
        @Schema(description = "최종 결제 금액 요약")
        OrderSummaryResponse summary
) {
        public record CheckoutItemDetailResponse(
                @Schema(description = "상품 ID")
                Long productId,
                @Schema(description = "sku ID")
                Long skuId,
                @Schema(description = "브랜드명")
                String brandName,
                @Schema(description = "상품명")
                String productName,
                @Schema(description = "선택 옵션 정보")
                String optionDescription,
                @Schema(description = "상품 대표 이미지 URL")
                String productImageUrl,
                @Schema(description = "상품 정가")
                Long basePrice,
                @Schema(description = "상품 판매가")
                Long salePrice,
                @Schema(description = "장바구니에 담은 수량")
                int quantity,
                @Schema(description = "총 정가")
                Long totalBasePrice,
                @Schema(description = "총 판매가")
                Long totalSalePrice
        ) {
                public static CheckoutItemDetailResponse of(Long productId, Long skuId, String brandName, String productName,
                                                      String optionDescription, String productImageUrl,
                                                      Long basePrice, Long salePrice, int quantity, Long totalBasePrice, Long totalSalePrice) {
                        return new CheckoutItemDetailResponse(
                                productId,
                                skuId,
                                brandName,
                                productName,
                                optionDescription,
                                productImageUrl,
                                basePrice,
                                salePrice,
                                quantity,
                                totalBasePrice,
                                totalSalePrice
                        );
                }
        }
        public record PointInfoResponse(
                @Schema(description = "보유 포인트")
                long availablePoints,
                @Schema(description = "최소 사용 가능 포인트")
                long minUsePoints,
                @Schema(description = "포인트 사용 단위")
                long pointUnit
        ) {
                public static PointInfoResponse of(long availablePoints, long minUsePoints, long pointUnit) {
                        return new PointInfoResponse(
                                availablePoints,
                                minUsePoints,
                                pointUnit
                        );
                }
        }
        public record PaymentMethodResponse(
                @Schema(description = "결제 수단 코드")
                String code,
                @Schema(description = "결제 수단 설명")
                String description,
                @Schema(description = "활성화 여부")
                boolean isEnabled
        ) {
                public static PaymentMethodResponse of(String code, String description, boolean isEnabled) {
                        return new PaymentMethodResponse(
                                code,
                                description,
                                isEnabled
                        );
                }
        }

        public static OrderCheckoutResponse of(
                DeliveryAddressResponse deliveryAddress,
                List<CheckoutItemDetailResponse> items,
                PointInfoResponse pointInfo,
                List<PaymentMethodResponse> paymentMethods,
                OrderSummaryResponse summary
        ) {
                return new OrderCheckoutResponse(
                        deliveryAddress,
                        items,
                        pointInfo,
                        paymentMethods,
                        summary
                );
        }
}