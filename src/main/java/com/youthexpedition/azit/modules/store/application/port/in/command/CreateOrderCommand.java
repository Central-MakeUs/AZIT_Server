package com.youthexpedition.azit.modules.store.application.port.in.command;

import java.util.List;

public record CreateOrderCommand(
        Long memberId,
        List<Long> cartItemIds,
        Long skuId,
        Integer quantity,
        String recipientName,
        String phoneNumber,
        String baseAddress,
        String detailAddress,
        String shippingInstruction,
        long membershipDiscount,
        long usedPoints,
        String paymentMethod
) {
    public static CreateOrderCommand of(Long memberId, List<Long> cartItemIds, Long skuId, Integer quantity,String recipientName, String phoneNumber,
                                        String baseAddress, String detailAddress, String shippingInstruction, long membershipDiscount, long usedPoints,
                                        String paymentMethod
    ) {
        return new CreateOrderCommand(memberId, cartItemIds, skuId, quantity, recipientName, phoneNumber, baseAddress, detailAddress,
                shippingInstruction, membershipDiscount, usedPoints, paymentMethod
        );
    }
}
