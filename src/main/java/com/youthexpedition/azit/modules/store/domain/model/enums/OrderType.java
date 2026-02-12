package com.youthexpedition.azit.modules.store.domain.model.enums;

import com.youthexpedition.azit.modules.store.application.port.in.command.CreateOrderCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
    CART, DIRECT, INVALID;

    public static OrderType from(CreateOrderCommand command) {
        if (command.cartItemIds() != null && !command.cartItemIds().isEmpty()) {
            return OrderType.CART;
        }
        if (command.skuId() != null && command.quantity() != null) {
            return OrderType.DIRECT;
        }
        return OrderType.INVALID;
    }
}
