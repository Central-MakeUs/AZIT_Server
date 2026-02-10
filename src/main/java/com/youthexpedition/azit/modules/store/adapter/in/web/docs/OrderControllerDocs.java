package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.adapter.in.web.dto.CreateOrderRequest;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Order" , description = "주문/결제 API")
public interface OrderControllerDocs {

    @Operation(
            summary = "주문서 조회(장바구니)",
            description = """
            장바구니에서 선택한 상품들을 바탕으로 주문서(결제 상세 페이지)에 필요한 정보를 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 파라미터 deliveryAddressId가 있을 경우, deliveryAddressId로 배송지 정보를 조회합니다.
            * 파라미터 deliveryAddressId가 없을 경우, 사용자의 기본 배송지 정보를 조회합니다. (없을 경우 null 반환)
            * 브랜드별로 가장 높은 배송비를 한 번씩만 합산하여 계산합니다.
            * 보유 포인트와 함께 사용 가능한 최소 단위(1,000P), 입력 단위(100P) 정보를 제공합니다.
            * 현재 사용 가능한 결제 수단 목록과 활성화 여부를 제공합니다.
            * '아지트 멤버십 할인'은 각 상품의 (정가 - 판매가) 총합으로 계산됩니다.
            * 포인트 '모두 사용' 클릭 시 응답의 availablePoints 값을 활용하여 100P 단위로 가공하시면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND", "SKU_NOT_FOUND", "MEMBER_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<OrderCheckoutResponse> getCheckoutInfoFromCart(
            @CurrentMemberId Long memberId, @RequestParam List<Long> cartItemIds, @RequestParam(required = false) Long deliveryAddressId);

    @Operation(
            summary = "주문서 조회(바로 구매)",
            description = """
            상품 상세에서 선택한 상품을 바탕으로 주문서(결제 상세 페이지)에 필요한 정보를 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 파라미터 deliveryAddressId가 있을 경우, deliveryAddressId로 배송지 정보를 조회합니다.
            * 파라미터 deliveryAddressId가 없을 경우, 사용자의 기본 배송지 정보를 조회합니다. (없을 경우 null 반환)
            * 사용자의 기본 배송지 정보를 조회합니다. (없을 경우 null 반환)
            * 브랜드별로 가장 높은 배송비를 한 번씩만 합산하여 계산합니다.
            * 보유 포인트와 함께 사용 가능한 최소 단위(1,000P), 입력 단위(100P) 정보를 제공합니다.
            * 현재 사용 가능한 결제 수단 목록과 활성화 여부를 제공합니다. (네이버페이는 현재 비활성화 상태)
            * '아지트 멤버십 할인'은 각 상품의 (정가 - 판매가) 총합으로 계산됩니다.
            * 포인트 '모두 사용' 클릭 시 응답의 availablePoints 값을 활용하여 100P 단위로 가공하시면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND", "SKU_NOT_FOUND", "MEMBER_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<OrderCheckoutResponse> getCheckoutInfoDirect(
            @CurrentMemberId Long memberId, @RequestParam Long skuId, @RequestParam Integer quantity, @RequestParam(required = false) Long deliveryAddressId);

    @Operation(
            summary = "주문 생성 (결제)",
            description = """
            최종 결제 정보와 배송지를 입력받아 주문을 생성하고 상품 재고를 차감합니다. <br><br>
            
            **[참고사항]** <br>
            * 주문 시점에 각 SKU의 재고가 즉시 차감됩니다.
            * 보유 포인트 잔액과 최소 사용 단위(1,000P)를 검증합니다. (INVALID_POINT_USAGE)
            * 결제 수단 (MVP): 현재 '무통장 입금(BANK_TRANSFER)'만 지원합니다. 그 외 수단은 에러를 반환합니다. (PAYMENT_METHOD_NOT_SUPPORTED)
            * 주문이 성공하면 선택한 장바구니 아이템들은 자동으로 삭제됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND", "OUT_OF_STOCK", "INVALID_POINT_USAGE", "INVALID_QUANTITY", "PAYMENT_METHOD_NOT_SUPPORTED", "INVALID_ORDER_REQUEST",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CreateOrderResponse> createOrder(@CurrentMemberId Long memberId, @RequestBody @Valid CreateOrderRequest request);
}
