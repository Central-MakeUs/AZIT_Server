package com.youthexpedition.azit.modules.store.application.port.in.command;

public record AddToCartCommand(
        Long memberId,
        Long productId,
        Long productSkuId,
        int quantity
) {
    public static AddToCartCommand of(Long memberId, Long productId, Long productSkuId, int quantity) {
        return new AddToCartCommand(memberId, productId, productSkuId, quantity);
    }
}
