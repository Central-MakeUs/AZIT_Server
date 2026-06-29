package com.youthexpedition.azit.modules.image.domain.model.enums;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
    UNSUPPORTED_FILE_EXTENSION("UNSUPPORTED_FILE_EXTENSION", "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 허용)", HttpStatus.BAD_REQUEST),
    INVALID_FILE_NAME("INVALID_FILE_NAME", "올바르지 않은 파일명입니다.", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_UPLOADED("IMAGE_NOT_UPLOADED", "이미지가 업로드되지 않았습니다. 먼저 이미지를 업로드해 주세요.", HttpStatus.BAD_REQUEST),
    IMAGE_OWNERSHIP_MISMATCH("IMAGE_OWNERSHIP_MISMATCH", "본인이 업로드한 이미지만 사용할 수 있습니다.", HttpStatus.FORBIDDEN),
    CREW_ID_REQUIRED("CREW_ID_REQUIRED", "크루 이미지 업로드 시 crewId는 필수입니다.", HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
