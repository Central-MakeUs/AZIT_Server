package com.youthexpedition.azit.modules.location.domain.model;

import lombok.Builder;

@Builder
public record LocationSearchResult(
        String placeName,
        String category,
        String address,
        Double latitude,
        Double longitude
) {
    public static LocationSearchResult of(String placeName, String category, String address, Double latitude, Double longitude) {
        return LocationSearchResult.builder()
                .placeName(placeName)
                .category(category)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
