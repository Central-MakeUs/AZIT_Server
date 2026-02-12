package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
