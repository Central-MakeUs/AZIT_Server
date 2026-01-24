package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Brand {
    private final Long id;
    private final String name;
    private final String logoImageUrl;
    private final String description;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;
}
