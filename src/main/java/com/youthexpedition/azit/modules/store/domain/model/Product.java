package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Product {
    private final Long id;
    private final Brand brand;
    private String name;
    private Long basePrice;
    private Integer discountRate;
    private Long salePrice;
    private Long shippingFee;
    private Integer shippingLeadTime;
    private String refundPolicy;
    private String description;
    private List<ProductImage> images;
    private List<ProductOptionGroup> optionGroups;
    private List<ProductSku> skus;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;

    // 예상 배송일 계산
    // 인스턴스 메서드 (엔티티 객체가 있을 때 사용)
    public LocalDate calculateExpectedShippingDate() {
        return calculateExpectedShippingDate(this.shippingLeadTime);
    }

    // 예상 배송일 계산
    // 정적 메서드 (DTO처럼 데이터만 있을 때 사용)
    public static LocalDate calculateExpectedShippingDate(int shippingLeadTime) {
        LocalDate expectedDay = LocalDate.now().plusDays(shippingLeadTime);

        if (expectedDay.getDayOfWeek() == DayOfWeek.SATURDAY) return expectedDay.plusDays(2);
        if (expectedDay.getDayOfWeek() == DayOfWeek.SUNDAY) return expectedDay.plusDays(1);

        return expectedDay;
    }
}
