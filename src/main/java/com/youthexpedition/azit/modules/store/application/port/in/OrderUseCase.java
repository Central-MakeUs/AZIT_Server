package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.modules.store.application.port.in.command.CreateOrderCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderDetailResponse;

import java.util.List;

public interface OrderUseCase {
    OrderCheckoutResponse getCheckoutInfoFromCart(Long memberId, List<Long> cartItemIds, Long deliveryAddressId);
    OrderCheckoutResponse getCheckoutInfoDirect(Long memberId, Long skuId, Integer quantity, Long deliveryAddressId);
    CreateOrderResponse createOrder(CreateOrderCommand command);
    OrderDetailResponse getOrderDetail(Long memberId, String orderNumber);
}
