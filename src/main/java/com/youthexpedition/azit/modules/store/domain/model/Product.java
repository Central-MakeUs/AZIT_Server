package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Product {
    private final Long id;
    private final String brandName;
    private final String name;
    private final Long basePrice;
    private final Integer discountRate;
    private final Long salePrice;
    private final Long shippingFee;
    private final String shippingPolicy;
    private final String refundPolicy;
    private final String description;
    private final List<ProductImage> images;
    private final List<ProductOption> options;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
