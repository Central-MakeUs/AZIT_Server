package com.youthexpedition.azit.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    // 시스템 공통 에러
    INTERNAL_SERVER_ERROR("COMMON_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("COMMON_002", "유효하지 않은 입력 값입니다.", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ERROR("COMMON_003", "사용자 권한이 없습니다.", HttpStatus.FORBIDDEN),
    BAD_REQUEST_ERROR("COMMON_004", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("COMMON_005", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    TYPE_MISMATCH_ERROR("COMMON_006", "데이터 타입이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // User 에러
    USER_NOT_FOUND("USER_001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),

    // Auth 에러
    UNAUTHORIZED("AUTH_001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}