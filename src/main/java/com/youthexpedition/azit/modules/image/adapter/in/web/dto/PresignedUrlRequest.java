package com.youthexpedition.azit.modules.image.adapter.in.web.dto;

import com.youthexpedition.azit.modules.image.application.port.in.command.GeneratePresignedUrlCommand;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @Schema(description = "이미지 업로드 타입", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "이미지 업로드 타입은 필수입니다.")
        ImageUploadType type,

        @Schema(description = "업로드할 파일명 (확장자 포함)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @Schema(description = "크루 ID (type이 CREW_IMAGE일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long crewId
) {
    public GeneratePresignedUrlCommand toCommand(Long memberId) {
        return GeneratePresignedUrlCommand.of(type, fileName, memberId, crewId);
    }
}
