package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCrewRequest(
        @Schema(description = "크루 이름")
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 15, message = "크루 이름은 최대 15자까지 가능합니다.")
        String name,

        @Schema(description = "크루 카테고리")
        @NotNull(message = "카테고리는 필수입니다.")
        String category,

        @Schema(description = "크루 활동 지역")
        @NotNull(message = "지역은 필수입니다.")
        String region
) {
    public CreateCrewCommand toCommand(Long memberId) {
        return CreateCrewCommand.of(name, category, region, memberId);
    }
}
