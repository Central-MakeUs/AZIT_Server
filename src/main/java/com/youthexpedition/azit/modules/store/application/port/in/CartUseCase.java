package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartListResponse;

public interface CartUseCase {
    void addOrUpdateCartItem(AddToCartCommand command);
    void deleteCartItems(Long memberId, CartItemDeleteCommand command);
    CartItemCountResponse getCartItemCount(Long memberId);
    CartListResponse getCarts(Long memberId);
}
