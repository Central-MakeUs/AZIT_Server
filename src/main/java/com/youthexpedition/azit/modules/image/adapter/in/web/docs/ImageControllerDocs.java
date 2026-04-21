package com.youthexpedition.azit.modules.image.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.image.adapter.in.web.dto.PresignedUrlRequest;
import com.youthexpedition.azit.modules.image.application.port.in.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Image", description = "이미지 업로드 API")
public interface ImageControllerDocs {

    @Operation(
            summary = "이미지 업로드용 Presigned URL 발급",
            description = """
                    S3에 이미지를 직접 업로드하기 위한 Presigned URL을 발급합니다. <br><br>

                    **[전체 플로우]** <br>
                    1. 해당 API를 호출하여 presignedUrl과 imageUrl을 발급받습니다. <br>
                    2. 클라이언트에서 presignedUrl로 **PUT 요청**을 보내 이미지를 업로드합니다. <br>
                    3. 업로드 완료 후 imageUrl으로 프로필 수정 / 크루 이미지 수정 등 관련 API를 호출합니다. <br><br>

                    **[업로드 타입]** <br>
                    * MEMBER_PROFILE : 멤버 프로필 이미지 (crewId 불필요) <br>
                    * CREW_IMAGE : 크루 이미지 (crewId 필수) <br><br>

                    **[제약 사항]** <br>
                    * 허용 확장자: jpg, jpeg, png, webp (그 외: UNSUPPORTED_FILE_EXTENSION) <br>
                    * Presigned URL 유효 시간: 5분 <br>
                    * 확장자 파싱이 불가능한 올바르지 않은 파일명일 경우 INVALID_FILE_NAME 에러가 발생합니다.
                    """
    )
    @ApiErrorCodeExamples({
            "UNSUPPORTED_FILE_EXTENSION", "INVALID_FILE_NAME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<PresignedUrlResponse> generatePresignedUrl(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Valid @RequestBody PresignedUrlRequest request
    );
}
