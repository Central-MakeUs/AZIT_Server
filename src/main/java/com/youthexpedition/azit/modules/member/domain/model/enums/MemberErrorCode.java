package com.youthexpedition.azit.modules.member.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    DELIVERY_ADDRESS_NOT_FOUND("DELIVERY_ADDRESS_NOT_FOUND", "존재하지 않는 주소입니다.", HttpStatus.NOT_FOUND),
    REQUIRED_TERMS_NOT_AGREED("REQUIRED_TERMS_NOT_AGREED", "필수 약관에 모두 동의해야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_ALREADY_WITHDRAWN("MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 회원입니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_STATUS("INVALID_MEMBER_STATUS", "유효하지 않은 회원 상태입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", "포인트가 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_POINT_VALUE("INVALID_POINT_VALUE", "유효하지 않은 포인트 요청입니다.", HttpStatus.BAD_REQUEST),
    DEFAULT_IMAGE_NOT_FOUND("DEFAULT_IMAGE_NOT_FOUND", "기본 이미지를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    JOINED_CREW_REQUIRED("JOINED_CREW_REQUIRED", "크루에 가입한 후 이용할 수 있는 서비스입니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;
}