package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.domain.model.Order;

import java.util.Optional;

public interface LoadOrderPort {
    boolean existsByOrderNumber(String orderNumber);
    Optional<Order> findByOrderNumber(String orderNumber);
    SliceResponse<Order> findOrdersByMemberId(Long memberId, CursorPageQuery query);
}
