package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CartItemListResponse(
        @Schema(description = "장바구니 항목 ID")
        Long id,
        @Schema(description = "브랜드 ID")
        Long brandId,
        @Schema(description = "브랜드명")
        String brandName,
        @Schema(description = "상품 ID")
        Long productId,
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
        boolean isOutOfStock,
        @Schema(description = "배송비")
        long shippingFee
) {
        public static CartItemListResponse of(Long id, Long brandId, String brandName, Long productId, String productName, LocalDate expectedShippingDate,
                                              Long productSkuId, String optionDescription, String productImageUrl,
                                              Long basePrice, Long salePrice, int quantity, boolean isOutOfStock, long shippingFee) {
                return new CartItemListResponse(
                        id, brandId, brandName, productId, productName, expectedShippingDate, productSkuId, optionDescription,
                        productImageUrl, basePrice, salePrice, quantity, isOutOfStock, shippingFee
                );
        }
}