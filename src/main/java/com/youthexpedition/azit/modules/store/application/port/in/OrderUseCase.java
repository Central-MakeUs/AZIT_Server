package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;

import java.util.List;

public interface OrderUseCase {
    OrderCheckoutResponse getCheckoutInfo(Long memberId, List<Long> cartItemIds);
}
