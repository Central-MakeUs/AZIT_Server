package com.youthexpedition.azit.modules.store.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}