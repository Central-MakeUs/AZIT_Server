package com.youthexpedition.azit.infrastructure.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    // 시스템 공통 에러
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "유효하지 않은 입력 값입니다.", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ERROR("FORBIDDEN_ERROR", "사용자 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_MEMBER_STATUS("INVALID_MEMBER_STATUS", "유효하지 않은 회원 상태입니다.", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    TYPE_MISMATCH_ERROR("TYPE_MISMATCH_ERROR", "데이터 타입이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}