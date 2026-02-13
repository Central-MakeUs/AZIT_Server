package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemListResponse;

import java.util.List;

public interface CartUseCase {
    void addOrUpdateCartItem(AddToCartCommand command);
    void updateCartItemQuantity(Long memberId, Long cartItemId, int quantity);
    void deleteCartItems(Long memberId, CartItemDeleteCommand command);
    CartItemCountResponse getCartItemCount(Long memberId);
    List<CartItemListResponse> getCarts(Long memberId);
}
