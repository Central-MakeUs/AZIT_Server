package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

public record LocationInfoResponse(
        @Schema(description = "장소명")
        String location_name,
        @Schema(description = "상세 주소")
        String address,
        @Schema(description = "세부 장소")
        String detailedLocation,
        @Schema(description = "위도")
        Double latitude,
        @Schema(description = "경도")
        Double longitude
) {
        public static LocationInfoResponse of(Location location) {
                return new LocationInfoResponse(
                        location.getName(),
                        location.getAddress(),
                        location.getDetailedLocation(),
                        location.getLatitude(),
                        location.getLongitude()
                );
        }
}