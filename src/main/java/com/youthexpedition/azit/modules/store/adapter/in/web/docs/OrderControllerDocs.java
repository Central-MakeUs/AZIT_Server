package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Order" , description = "주문/결제 API")
public interface OrderControllerDocs {

    @Operation(
            summary = "주문/결제 상세 정보 조회",
            description = """
            장바구니에서 선택한 상품들을 바탕으로 결제 상세 페이지에 필요한 정보를 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 배송지: 사용자의 기본 배송지 정보를 조회합니다. (없을 경우 null 반환)
            * 배송비: 브랜드별로 가장 높은 배송비를 한 번씩만 합산하여 계산합니다.
            * 포인트 정책: 보유 포인트와 함께 사용 가능한 최소 단위(1,000P), 입력 단위(100P) 정보를 제공합니다.
            * 결제 수단: 현재 사용 가능한 결제 수단 목록과 활성화 여부를 제공합니다. (네이버페이는 현재 비활성화 상태)
            * '아지트 멤버십 할인'은 각 상품의 (정가 - 판매가) 총합으로 계산됩니다.
            * 포인트 '모두 사용' 클릭 시 응답의 availablePoints 값을 활용하여 100P 단위로 가공하시면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND", "SKU_NOT_FOUND", "MEMBER_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<OrderCheckoutResponse> getCheckoutInfo(@CurrentMemberId Long memberId, @RequestParam List<Long> cartItemId);
}
