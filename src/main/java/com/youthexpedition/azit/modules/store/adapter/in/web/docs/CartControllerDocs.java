package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.AddToCartRequest;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.CartItemDeleteRequest;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.UpdateCartItemQuantityRequest;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Cart" , description = "장바구니 API")
public interface CartControllerDocs {

    @Operation(
            summary = "장바구니 담기 및 수량 추가(상품 상세)",
            description = """
            상품 상세 페이지에서 상품의 특정 옵션(SKU)을 장바구니에 담거나 기존 항목의 수량을 추가합니다. <br><br>
            
            **[참고 사항]** <br>
            * 장바구니에 해당 SKU가 없는 경우 새로운 항목으로 등록됩니다.
            * 이미 동일한 SKU가 장바구니에 있는 경우, 기존 수량에 요청한 수량만큼 더해집니다.
            * 담으려는 총 수량이 상품의 실제 재고 수량을 초과할 경우 에러가 발생합니다. (OUT_OF_STOCK)
            * 수량은 최소 1개 이상이어야 합니다. (INVALID_QUANTITY)
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND", "SKU_NOT_FOUND", "OUT_OF_STOCK", "INVALID_QUANTITY",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> addCartItem(@Parameter(hidden = true) @CurrentMemberId Long memberId, @RequestBody @Valid AddToCartRequest request);

    @Operation(
            summary = "장바구니 아이템 수량 변경",
            description = """
                장바구니에 담긴 특정 아이템의 수량을 변경(절대값)합니다. <br><br>
                
                **[참고 사항]** <br>
                * 변경하려는 수량이 상품의 재고보다 많을 수 없습니다. (OUT_OF_STOCK)
                * 변경하려는 수량은 최소 1개 이상이어야 합니다. (INVALID_QUANTITY)
                * 본인의 장바구니 항목만 수정할 수 있습니다. (FORBIDDEN_ERROR)
                """
    )
    @ApiErrorCodeExamples({
            "CART_ITEM_NOT_FOUND", "OUT_OF_STOCK", "INVALID_QUANTITY", "FORBIDDEN_ERROR",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> updateCartItemQuantity(
            @Parameter(hidden = true) @CurrentMemberId Long memberId, @PathVariable Long cartItemId, @RequestBody @Valid UpdateCartItemQuantityRequest request);

    @Operation(
            summary = "장바구니 항목 삭제",
            description = """
            선택한 장바구니 항목들을 삭제합니다. <br><br>
            
            **[동작 방식]** <br>
            * 단건 삭제: 리스트에 하나의 ID만 담아 요청합니다. (예: {"cartItemIds": [1]})
            * 다건 삭제: 삭제할 모든 ID를 리스트에 담아 요청합니다. (예: {"cartItemIds": [1, 2, 3]})
            
            **[참고 사항]** <br>
            * 로그인한 사용자의 장바구니 항목만 삭제할 수 있습니다. 타인의 ID를 포함하더라도 해당 항목은 무시되고 본인의 것만 삭제됩니다.
            * 멱등성 보장을 위해 이미 삭제된 ID나 존재하지 않는 ID를 요청에 포함하더라도 에러를 발생시키지 않고 성공 처리합니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> deleteItems(@Parameter(hidden = true) @CurrentMemberId Long memberId, @RequestBody CartItemDeleteRequest request);

    @Operation(
            summary = "장바구니 수량 조회",
            description = """
            장바구니 아이콘에 표시될 아이템 종류 수를 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 상품의 총 수량 합계가 아닌 장바구니에 담긴 '품목'의 개수를 반환합니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CartItemCountResponse> getCartItemCount(@Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "장바구니 목록 조회",
            description = """
            사용자의 장바구니에 담긴 상품 목록을 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * expectedShippingDate: 상품의 예상 출고 소요 시간을 기준으로 계산되며, 주말(토, 일)은 발송일에서 제외됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<CartItemListResponse>> getCarts(@Parameter(hidden = true) @CurrentMemberId Long memberId);
}
