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
    private String imageUrl;
    private Integer sortOrder;
    private ProductImageType imageType;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;

    public void updateImageUrl(String fullImageUrl) {
        this.imageUrl = fullImageUrl;
    }
}
