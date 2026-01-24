package com.youthexpedition.azit.infrastructure.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL) // null 값이면 JSON 응답에서 제외
public record CommonErrorResponse(String code, String message) {
    public static CommonErrorResponse of(BaseErrorCode errorCode) {
        return new CommonErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static CommonErrorResponse of(String code, String message) {
        return new CommonErrorResponse(code, message);
    }
}
