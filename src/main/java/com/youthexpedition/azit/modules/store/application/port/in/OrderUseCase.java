package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.command.CreateOrderCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderListResponse;

import java.util.List;

public interface OrderUseCase {
    OrderCheckoutResponse getCheckoutInfoFromCart(Long memberId, List<Long> cartItemIds, Long deliveryAddressId);
    OrderCheckoutResponse getCheckoutInfoDirect(Long memberId, Long skuId, Integer quantity, Long deliveryAddressId);
    CreateOrderResponse createOrder(CreateOrderCommand command);
    OrderDetailResponse getOrderDetail(Long memberId, String orderNumber);
    SliceResponse<OrderListResponse> getOrders(Long memberId, CursorPageQuery query);
}
