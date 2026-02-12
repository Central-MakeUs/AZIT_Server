package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.modules.store.domain.model.CartItem;

import java.util.List;

public interface SaveCartPort {
    void save(CartItem cartItem);
    void addQuantity(Long cartItemId, int quantity);
    void deleteAllByMemberIdAndIds(Long memberId, List<Long> cartItemIds);
}
