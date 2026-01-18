package com.youthexpedition.azit.modules.member.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    REQUIRED_TERMS_NOT_AGREED("REQUIRED_TERMS_NOT_AGREED", "필수 약관에 모두 동의해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_STATUS("INVALID_MEMBER_STATUS", "유효하지 않은 회원 상태입니다.", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_WITHDRAWN("MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 회원입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}