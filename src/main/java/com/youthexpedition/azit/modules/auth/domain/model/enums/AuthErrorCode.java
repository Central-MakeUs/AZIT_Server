package com.youthexpedition.azit.modules.auth.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNAUTHORIZED("AUTH_001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // 소셜 로그인
    INVALID_SOCIAL_CODE("AUTH_003", "잘못된 소셜 인가 코드입니다.", HttpStatus.BAD_REQUEST),
    SOCIAL_AUTHENTICATION_FAILED("AUTH_004", "소셜 인증에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}