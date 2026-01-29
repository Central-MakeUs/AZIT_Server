package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartItemQueryDto> findCartDetailsByMemberId(Long memberId);
}
