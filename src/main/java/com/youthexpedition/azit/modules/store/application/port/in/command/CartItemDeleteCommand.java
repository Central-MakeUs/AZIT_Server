package com.youthexpedition.azit.modules.store.application.port.in.command;

import java.util.List;

public record CartItemDeleteCommand(
        List<Long> cartItemIds
) {
    public static CartItemDeleteCommand of(List<Long> cartItemIds) {
        return new CartItemDeleteCommand(cartItemIds);
    }
}
