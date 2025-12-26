package com.youthexpedition.azit.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiSuccessCode {

    SUCCESS("SUCCESS", "요청에 성공했습니다.", HttpStatus.OK),
    CREATED("CREATED", "리소스가 성공적으로 생성되었습니다.", HttpStatus.CREATED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
