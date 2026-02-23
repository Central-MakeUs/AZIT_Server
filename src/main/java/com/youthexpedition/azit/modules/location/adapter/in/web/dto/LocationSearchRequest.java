package com.youthexpedition.azit.modules.location.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LocationSearchRequest(
        @Schema(description = "Apple에서 전달한 페이로드")
        @NotBlank(message = "페이로드는 필수입니다.")
        String payload
) {
}
