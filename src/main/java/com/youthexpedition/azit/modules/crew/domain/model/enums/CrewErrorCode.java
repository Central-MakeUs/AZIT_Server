package com.youthexpedition.azit.modules.crew.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CrewErrorCode implements BaseErrorCode {
    CREW_NOT_FOUND("CREW_NOT_FOUND", "존재하지 않는 크루입니다.", HttpStatus.NOT_FOUND),
    INVALID_CREW_CATEGORY("INVALID_CREW_CATEGORY", "유효하지 않은 크루 카테고리입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REGION("INVALID_REGION", "유효하지 않은 활동 지역입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_JOINED_CREW("ALREADY_JOINED_CREW", "이미 가입한 크루입니다.", HttpStatus.BAD_REQUEST),
    INVITATION_CODE_GENERATION_FAILED("INVITATION_CODE_GENERATION_FAILED", "초대 코드 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALREADY_PROCESSED_JOIN_REQUEST("ALREADY_PROCESSED_JOIN_REQUEST", "이미 승인 또는 거절된 가입 요청입니다.", HttpStatus.BAD_REQUEST),
    JOIN_REQUEST_NOT_FOUND("JOIN_REQUEST_NOT_FOUND", "존재하지 않는 가입 요청입니다.", HttpStatus.NOT_FOUND),
    NOT_CREW_LEADER("NOT_CREW_LEADER", "해당 크루의 리더 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_A_CREW_MEMBER("NOT_A_CREW_MEMBER", "해당 크루의 멤버가 아닙니다.", HttpStatus.FORBIDDEN),
    CREW_MEMBER_NOT_FOUND("CREW_MEMBER_NOT_FOUND", "가입한 크루가 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}