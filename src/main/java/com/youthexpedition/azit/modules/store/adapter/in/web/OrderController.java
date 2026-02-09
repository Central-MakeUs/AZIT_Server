package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.OrderControllerDocs;
import com.youthexpedition.azit.modules.store.application.port.in.OrderUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
