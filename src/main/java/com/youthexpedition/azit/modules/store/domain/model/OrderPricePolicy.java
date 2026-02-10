package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.modules.store.application.port.out.query.PriceCalculateDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // 유틸리티 클래스이므로 파라미터가 없는 기본 생성자 private으로 생성
public final class OrderPricePolicy {

    // 배송비 계산 (브랜드별 최대 배송비 합산)
    public static long calculateTotalShippingFee(List<? extends PriceCalculateDto> items) {
        Map<Long, Long> brandMaxShippingFeesMap = new HashMap<>();
        for (PriceCalculateDto item : items) {
            brandMaxShippingFeesMap.merge(item.brandId(), item.shippingFee(), Long::max);
        }
        return brandMaxShippingFeesMap.values().stream().mapToLong(Long::longValue).sum();
    }

    // 총 상품 금액 (정가 합계) 계산
    public static long calculateTotalProductPrice(List<? extends PriceCalculateDto> items) {
        return items.stream()
                .mapToLong(item -> (item.basePrice() + item.additionalPrice()) * item.quantity())
                .sum();
    }

    // 총 멤버십 할인 금액 계산
    public static long calculateTotalMembershipDiscount(List<? extends PriceCalculateDto> items) {
        return items.stream()
                .mapToLong(item -> (item.basePrice() - item.salePrice()) * item.quantity())
                .sum();
    }
}
