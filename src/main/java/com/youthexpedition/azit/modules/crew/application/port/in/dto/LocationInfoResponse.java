package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

public record LocationInfoResponse(
        @Schema(description = "집합 장소명")
        String placeName,
        @Schema(description = "주소")
        String address,
        @Schema(description = "모이는 지점")
        String meetingSpot,
        @Schema(description = "위도")
        Double latitude,
        @Schema(description = "경도")
        Double longitude
) {
        public static LocationInfoResponse of(Location location) {
                return new LocationInfoResponse(
                        location.getPlaceName(),
                        location.getAddress(),
                        location.getMeetingSpot(),
                        location.getLatitude(),
                        location.getLongitude()
                );
        }
}