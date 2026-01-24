package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProductImage {
    private final Long id;
    private final String imageUrl;
    private final Integer sortOrder;
    private final ProductImageType imageType; // SLIDE, DETAIL
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
