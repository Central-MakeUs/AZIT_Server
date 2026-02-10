package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartItemQueryDto> findCartDetailsByMemberId(Long memberId);
    List<CheckoutItemDto> findCartDetailsByIds(List<Long> cartItemIds);
}
