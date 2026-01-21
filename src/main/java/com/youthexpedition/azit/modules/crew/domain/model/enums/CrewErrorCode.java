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
    INVITATION_CODE_GENERATION_FAILED("INVITATION_CODE_GENERATION_FAILED", "이미 가입한 크루입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}