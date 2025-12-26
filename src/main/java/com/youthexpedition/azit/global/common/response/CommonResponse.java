package com.youthexpedition.azit.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.youthexpedition.azit.global.common.response.code.CommonSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값이면 JSON 응답에서 제외
public class CommonResponse<T> {
    private final String code;
    private final String message;
    private final T result;

    public static <T> CommonResponse<T> of(CommonSuccessCode code, T result) {
        return new CommonResponse<>(code.getCode(), code.getMessage(), result);
    }

    public static <T> CommonResponse<T> of(CommonSuccessCode code) {
        return new CommonResponse<>(code.getCode(), code.getMessage(), null);
    }
}
