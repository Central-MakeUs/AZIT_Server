package com.youthexpedition.azit.modules.location.application.port.service.mapper;

import com.youthexpedition.azit.modules.location.application.port.in.dto.LocationSearchResponse;
import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationResponseMapper {

    public List<LocationSearchResponse> toSearchResponseList(List<LocationSearchResult> results) {
        return results.stream()
                .map(this::toSearchResponse)
                .toList();
    }

    private LocationSearchResponse toSearchResponse(LocationSearchResult result) {
        return new LocationSearchResponse(
                result.placeName(),
                result.category(),
                result.address(),
                result.latitude(),
                result.longitude()
        );
    }
}
