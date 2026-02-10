package com.youthexpedition.azit.modules.store.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {
    // 상품 관련
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND),
    SKU_NOT_FOUND("SKU_NOT_FOUND", "해당 상품의 옵션 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK("OUT_OF_STOCK", "재고가 부족합니다.", HttpStatus.BAD_REQUEST),

    // 포인트 사용 관련
    INVALID_POINT_UNIT("INVALID_POINT_UNIT", "포인트는 100P 단위로 이용이 가능합니다.", HttpStatus.BAD_REQUEST),
    BELOW_MIN_POINT_USAGE("BELOW_MIN_POINT_USAGE", "포인트는 1000P부터 사용이 가능합니다.", HttpStatus.BAD_REQUEST),
    EXCEED_AVAILABLE_POINTS("EXCEED_AVAILABLE_POINTS", "보유한 포인트보다 많은 포인트를 사용할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 주문/결제 관련
    INVALID_QUANTITY("INVALID_QUANTITY", "주문 수량은 1개 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_NOT_SUPPORTED("PAYMENT_METHOD_NOT_SUPPORTED", "지원하지 않는 결제 수단입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_REQUEST("INVALID_ORDER_REQUEST", "유효하지 않은 결제 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}