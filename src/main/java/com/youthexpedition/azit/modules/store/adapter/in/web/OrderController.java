package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.OrderControllerDocs;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.CreateOrderRequest;
import com.youthexpedition.azit.modules.store.application.port.in.OrderUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {
    private final OrderUseCase orderUseCase;

    @GetMapping("/checkout/cart")
    public CommonResponse<OrderCheckoutResponse> getCheckoutInfoFromCart(
            @CurrentMemberId Long memberId, @RequestParam List<Long> cartItemIds, @RequestParam(required = false) Long deliveryAddressId) {
        OrderCheckoutResponse response = orderUseCase.getCheckoutInfoFromCart(memberId, cartItemIds, deliveryAddressId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/checkout/direct")
    public CommonResponse<OrderCheckoutResponse> getCheckoutInfoDirect(
            @CurrentMemberId Long memberId, @RequestParam Long skuId, @RequestParam Integer quantity, @RequestParam(required = false) Long deliveryAddressId) {
        OrderCheckoutResponse response = orderUseCase.getCheckoutInfoDirect(memberId, skuId, quantity, deliveryAddressId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PostMapping
    public CommonResponse<CreateOrderResponse> createOrder(@CurrentMemberId Long memberId, @RequestBody @Valid CreateOrderRequest request) {
        CreateOrderResponse response = orderUseCase.createOrder(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/{orderNumber}")
    public CommonResponse<OrderDetailResponse> getOrderDetail(@CurrentMemberId Long memberId, @PathVariable String orderNumber) {
        OrderDetailResponse response = orderUseCase.getOrderDetail(memberId, orderNumber);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
