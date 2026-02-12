package com.youthexpedition.azit.modules.store.adapter.in.web.dto;

import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartItemDeleteRequest(
        @Schema(description = "삭제할 장바구니 ID 리스트")
        @NotEmpty(message = "삭제할 장바구니 ID는 필수입니다.")
        List<Long> cartItemIds
) {
    public CartItemDeleteCommand toCommand() {
        return CartItemDeleteCommand.of(cartItemIds.stream().filter(java.util.Objects::nonNull).toList()); // null 요소 필터링
    }
}
