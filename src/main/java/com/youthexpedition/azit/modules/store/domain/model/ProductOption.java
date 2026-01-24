package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProductOption {
    private final Long id;
    private final String optionName; // 옵션 이름 (사이즈 등)
    private final String optionValue; // 옵션값 (230, 240)
    private final Long additionalPrice;
    private final Integer stockQuantity;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
