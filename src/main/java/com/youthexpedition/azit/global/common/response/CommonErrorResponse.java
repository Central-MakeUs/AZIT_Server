package com.youthexpedition.azit.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.youthexpedition.azit.global.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값이면 JSON 응답에서 제외
public class CommonErrorResponse {
    private final String code;
    private final String message;

    public static CommonErrorResponse of(BaseErrorCode errorCode) {
        return new CommonErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static CommonErrorResponse of(String code, String message) {
        return new CommonErrorResponse(code, message);
    }
}
