package com.youthexpedition.azit.modules.auth.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNAUTHORIZED("AUTH_001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}