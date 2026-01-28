package com.youthexpedition.azit.modules.store.adapter.in.web.dto;

import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @Schema(description = "상품 ID")
        @NotNull
        Long productId,
        @Schema(description = "상품 옵션 조합 ID")
        @NotNull
        Long productSkuId,
        @Schema(description = "담을 수량")
        @Min(1)
        int quantity
) {
    public AddToCartCommand toCommand(Long memberId) {
        return AddToCartCommand.of(memberId, productId, productSkuId, quantity);
    }
}
