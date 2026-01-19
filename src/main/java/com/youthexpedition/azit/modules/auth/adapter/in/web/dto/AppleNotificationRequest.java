package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AppleNotificationRequest(
        @Schema(description = "Apple에서 전달한 서명된 페이로드 (JWS)")
        @NotBlank(message = "페이로드는 필수입니다.")
        String payload
) {
}