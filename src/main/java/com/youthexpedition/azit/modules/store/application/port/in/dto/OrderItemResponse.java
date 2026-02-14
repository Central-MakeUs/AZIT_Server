package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderItemResponse(
        @Schema(description = "카트 아이템 ID")
        Long id,
        @Schema(description = "상품 ID")
        Long productId,
        @Schema(description = "sku ID")
        Long skuId,
        @Schema(description = "구매 당시 브랜드명")
        String brandName,
        @Schema(description = "구매 당시 상품명")
        String productName,
        @Schema(description = "구매 당시 옵션 정보")
        String optionDescription,
        @Schema(description = "구매 당시 상품 대표 이미지 url")
        String productImageUrl,
        @Schema(description = "상품 정가")
        long basePrice,
        @Schema(description = "상품 판매가")
        long salePrice,
        @Schema(description = "총 판매가")
        long totalSalePrice,
        @Schema(description = "구매 수량")
        int quantity
) {
    public static OrderItemResponse of(Long id, Long productId, Long skuId, String brandName, String productName, String optionDescription,
                                       String productImageUrl, long basePrice, long salePrice, long totalSalePrice, int quantity) {
        return new OrderItemResponse(
                id,
                productId,
                skuId,
                brandName,
                productName,
                optionDescription,
                productImageUrl,
                basePrice,
                salePrice,
                totalSalePrice,
                quantity);
    }
}
