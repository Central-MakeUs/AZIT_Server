package com.youthexpedition.azit.modules.store.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND),
    SKU_NOT_FOUND("SKU_NOT_FOUND", "해당 상품의 옵션 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_QUANTITY("INVALID_QUANTITY", "유효하지 않은 수량입니다.", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK("OUT_OF_STOCK", "재고가 부족합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}