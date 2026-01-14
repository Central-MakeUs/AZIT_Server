package com.youthexpedition.azit.modules.crew.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CrewErrorCode implements BaseErrorCode {
    CREW_NOT_FOUND("CREW_NOT_FOUND", "존재하지 않는 크루입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}