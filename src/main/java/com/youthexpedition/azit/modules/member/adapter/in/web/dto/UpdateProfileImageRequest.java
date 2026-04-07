package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateProfileImageCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @Schema(description = "업로드 완료된 이미지의 CloudFront URL", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "https://azitcrew.com/profile/123/2026-04-07_550e8400.jpg")
        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl
) {
    public UpdateProfileImageCommand toCommand() {
        return UpdateProfileImageCommand.of(imageUrl);
    }
}
