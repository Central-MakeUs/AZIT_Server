package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.modules.store.domain.model.CartItem;

public interface SaveCartPort {
    void save(CartItem cartItem);
    void addQuantity(Long cartItemId, int quantity);
}
