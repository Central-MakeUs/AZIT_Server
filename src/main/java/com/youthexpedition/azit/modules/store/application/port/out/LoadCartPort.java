package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.modules.store.domain.model.CartItem;

import java.util.Optional;

public interface LoadCartPort {
    Optional<CartItem> findByMemberIdAndSkuId(Long memberId, Long productSkuId);
    long countByMemberId(Long memberId);
}
