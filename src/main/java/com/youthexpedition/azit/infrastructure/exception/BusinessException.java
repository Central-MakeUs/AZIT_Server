package com.youthexpedition.azit.infrastructure.exception;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public BusinessException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
