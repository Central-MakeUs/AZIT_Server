package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductListResponse(
        @Schema(description = "상품 ID")
        Long id,
        @Schema(description = "브랜드명")
        String brandName,
        @Schema(description = "상품명")
        String productName,
        @Schema(description = "기본 가격")
        Long basePrice,
        @Schema(description = "할인율")
        Integer discountRate,
        @Schema(description = "할인된 가격")
        Long salePrice,
        @Schema(description = "썸네일 이미지 url")
        String thumbnailImageUrl
) {
}
