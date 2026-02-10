package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record CartListResponse(
        @Schema(description = "장바구니 상품 목록")
        List<CartItemDetail> items,
        @Schema(description = "총 상품금액 (할인 전 합계)")
        long totalProductPrice,
        @Schema(description = "아지트 멤버십 할인 금액")
        long membershipDiscount,
        @Schema(description = "배송비")
        long shippingFee,
        @Schema(description = "최종 결제 예정 금액")
        long totalPaymentPrice
) {
    public record CartItemDetail(
            @Schema(description = "장바구니 항목 ID")
            Long cartItemId,
            @Schema(description = "브랜드명")
            String brandName,
            @Schema(description = "상품명")
            String productName,
            @Schema(description = "예상 발송 시작일")
            LocalDate expectedShippingDate,
            @Schema(description = "상품 sku ID")
            Long productSkuId,
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
            @Schema(description = "품절 여부")
            boolean isOutOfStock
    ) {
    }
        public static CartListResponse of(
                List<CartItemDetail> items, long totalProductPrice, long membershipDiscount, long shippingFee) {
                return new CartListResponse(
                        items,
                        totalProductPrice,
                        membershipDiscount,
                        shippingFee,
                        totalProductPrice - membershipDiscount + shippingFee
                );
        }
}