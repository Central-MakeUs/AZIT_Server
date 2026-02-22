package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CheckInCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @Schema(description = "위도")
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @Schema(description = "경도")
        @NotNull(message = "경도는 필수입니다.")
        Double longitude
) {
    public CheckInCommand toCommand(Long memberId, Long scheduleId) {
        return CheckInCommand.of(memberId, scheduleId, latitude, longitude);
    }
}
