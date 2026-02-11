package com.youthexpedition.azit.modules.store.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Schema(description = "담을 수량")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
}
