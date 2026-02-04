package com.youthexpedition.azit.modules.member.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements BaseErrorCode {
    ADDRESS_NOT_FOUND("ADDRESS_NOT_FOUND", "존재하지 않는 배송지입니다.", HttpStatus.NOT_FOUND),
    FORBIDDEN_ADDRESS_ACCESS("FORBIDDEN_ADDRESS_ACCESS", "해당 배송지 설정에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_ADDRESS_INPUT("INVALID_ADDRESS_INPUT", "배송지 필수 정보가 누락되었습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}