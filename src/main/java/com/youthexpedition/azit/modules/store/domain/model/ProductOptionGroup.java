package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductOptionGroup {
    private final Long id;
    private String name;
    private Integer sortOrder;
    private List<ProductOptionValue> values;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;
}
