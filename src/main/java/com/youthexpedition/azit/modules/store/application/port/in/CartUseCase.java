package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;

public interface CartUseCase {
    void addOrUpdateCartItem(AddToCartCommand command);
    void deleteCartItems(Long memberId, CartItemDeleteCommand command);
}
