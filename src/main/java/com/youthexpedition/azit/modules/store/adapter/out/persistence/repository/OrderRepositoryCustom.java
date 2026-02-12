package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderEntity;

public interface OrderRepositoryCustom {
    SliceResponse<OrderEntity> findOrdersByMemberId(Long memberId, CursorPageQuery query);
}
