package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;

public interface CartUseCase {
    void addOrUpdateCartItem(Long memberId, AddToCartCommand command);
}
