package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.CartControllerDocs;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.AddToCartRequest;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.CartItemDeleteRequest;
import com.youthexpedition.azit.modules.store.application.port.in.CartUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController implements CartControllerDocs {
    private final CartUseCase cartUseCase;

    @PostMapping("/items")
    public CommonResponse<Void> addCartItem(@CurrentMemberId Long memberId, @RequestBody @Valid AddToCartRequest request) {
        cartUseCase.addOrUpdateCartItem(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/items")
    public CommonResponse<Void> deleteItems(@CurrentMemberId Long memberId, @RequestBody CartItemDeleteRequest request) {
        cartUseCase.deleteCartItems(memberId, request.toCommand());

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/count")
    public CommonResponse<CartItemCountResponse> getCartItemCount(@CurrentMemberId Long memberId) {
        CartItemCountResponse result = cartUseCase.getCartItemCount(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }

    @GetMapping
    public CommonResponse<CartListResponse> getCarts(@CurrentMemberId Long memberId) {
        CartListResponse response = cartUseCase.getCarts(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
