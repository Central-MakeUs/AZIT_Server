package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.OrderControllerDocs;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.CreateOrderRequest;
import com.youthexpedition.azit.modules.store.application.port.in.OrderUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {
    private final OrderUseCase orderUseCase;

    @GetMapping("/checkout")
    public CommonResponse<OrderCheckoutResponse> getCheckoutInfo(@CurrentMemberId Long memberId, @RequestParam List<Long> cartItemIds) {
        OrderCheckoutResponse response = orderUseCase.getCheckoutInfo(memberId, cartItemIds);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PostMapping
    public CommonResponse<CreateOrderResponse> createOrder(@CurrentMemberId Long memberId, @RequestBody @Valid CreateOrderRequest request) {
        CreateOrderResponse response = orderUseCase.createOrder(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
