package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateCrewImageCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateCrewImageRequest(
        @Schema(description = "업로드 완료된 이미지의 CloudFront URL", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "https://azitcrew.com/temp/crew/1/2026-04-21_550e8400.jpg")
        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl
) {
    public UpdateCrewImageCommand toCommand() {
        return UpdateCrewImageCommand.of(imageUrl);
    }
}
