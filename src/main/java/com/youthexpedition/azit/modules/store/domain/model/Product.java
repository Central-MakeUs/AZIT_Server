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
    private String brandName;
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
}
