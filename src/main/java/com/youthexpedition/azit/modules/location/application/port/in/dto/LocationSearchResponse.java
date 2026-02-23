package com.youthexpedition.azit.modules.location.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocationSearchResponse(
        @Schema(description = "장소 명칭")
        String placeName,
        @Schema(description = "카테고리")
        String category,
        @Schema(description = "주소")
        String address,
        @Schema(description = "위도")
        Double latitude,
        @Schema(description = "경도")
        Double longitude
) {
}
