package com.youthexpedition.azit.modules.store.application.port.out.query;

import java.util.List;

// 주문서 및 주문/결제용
public record CheckoutItemDto(
        Long productId,
        Long skuId,
        String brandName,        // 브랜드 이름
        String productName,      // 상품 이름
        Integer shippingLeadTime, // 배송 소요 시간
        String imageUrl,         // 이미지 URL
        Long basePrice,          // 상품 정가
        Long salePrice,          // 상품 판매가
        Long additionalPrice,    // SKU별 추가 금액
        Integer quantity,        // 담은 수량
        Integer stockQuantity,   // SKU 재고 수량
        Long brandId,            // 브랜드별 배송비 계산을 위한 ID
        Long shippingFee,         // 배송비
        List<String> optionValues // 옵션 값
) implements PriceCalculateDto {
}
