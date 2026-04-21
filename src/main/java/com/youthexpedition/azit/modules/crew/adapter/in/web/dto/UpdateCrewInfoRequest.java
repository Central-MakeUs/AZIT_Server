package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateCrewInfoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCrewInfoRequest(
        @Schema(description = "크루 이름", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "크루 이름은 필수입니다.")
        @Size(max = 15, message = "크루 이름은 최대 15자까지 가능합니다.")
        String name,

        @Schema(description = "크루 한줄 소개 (선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 20, message = "크루 소개는 최대 20자까지 가능합니다.")
        String description
) {
    public UpdateCrewInfoCommand toCommand() {
        return UpdateCrewInfoCommand.of(name, description);
    }
}
