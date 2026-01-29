package com.youthexpedition.azit.modules.store.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemCountResponse(
        @Schema(description = "장바구니 아이템 종류 수")
        long count
) {
        public static CartItemCountResponse from(long count) {
                return new CartItemCountResponse(count);
        }
}