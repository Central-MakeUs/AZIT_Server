package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.AddToCartRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Cart" , description = "장바구니 API")
public interface CartControllerDocs {

    @Operation(
            summary = "장바구니 항목 추가 및 수량 변경",
            description = """
            상품의 특정 옵션(SKU)을 장바구니에 담거나 기존 항목의 수량을 추가합니다. <br><br>
            
            **[참고 사항]** <br>
            * 신규 추가: 장바구니에 해당 SKU가 없는 경우 새로운 항목으로 등록됩니다.
            * 수량 합산: 이미 동일한 SKU가 장바구니에 있는 경우, 기존 수량에 요청한 수량만큼 더해집니다.
            * 담으려는 총 수량이 상품의 실제 재고 수량을 초과할 경우 에러가 발생합니다. (OUT_OF_STOCK)
            * 수량은 최소 1개 이상이어야 합니다. (INVALID_QUANTITY)
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND", "SKU_NOT_FOUND", "OUT_OF_STOCK", "INVALID_QUANTITY",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> addCartItem(@CurrentMemberId Long memberId, @RequestBody @Valid AddToCartRequest request);
}
