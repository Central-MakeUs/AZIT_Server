package com.youthexpedition.azit.modules.auth.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNAUTHORIZED("AUTH_UNAUTHORIZED", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 소셜 로그인
    INVALID_SOCIAL_CODE("AUTH_INVALID_SOCIAL_CODE", "잘못된 소셜 인가 코드입니다.", HttpStatus.BAD_REQUEST),
    SOCIAL_AUTHENTICATION_FAILED("AUTH_SOCIAL_AUTHENTICATION_FAILED", "소셜 인증에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 애플 소셜 로그인
    INVALID_APPLE_ID_TOKEN("AUTH_INVALID_APPLE_ID_TOKEN", "애플 ID 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    APPLE_PUBLIC_KEY_NOT_FOUND("AUTH_APPLE_PUBLIC_KEY_NOT_FOUND", "유효한 애플 공개키를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    APPLE_CLIENT_SECRET_CREATION_FAILED("AUTH_APPLE_CLIENT_SECRET_FAILED", "애플 클라이언트 시크릿 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 토큰
    TOKEN_REUSE_DETECTED("AUTH_TOKEN_REUSE_DETECTED", "토큰 재사용이 감지되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH_EXPIRED_TOKEN", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}