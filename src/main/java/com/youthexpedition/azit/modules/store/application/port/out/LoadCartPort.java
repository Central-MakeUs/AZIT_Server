package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface LoadCartPort {
    Optional<CartItem> findByMemberIdAndSkuId(Long memberId, Long productSkuId);
    Optional<CartItem> findById(Long cartItemId);
    long countByMemberId(Long memberId);
    List<CartItemQueryDto> findCartDetailsByMemberId(Long memberId);
    List<CheckoutItemDto> findCartDetailsByIds(List<Long> cartItemIds);
}
