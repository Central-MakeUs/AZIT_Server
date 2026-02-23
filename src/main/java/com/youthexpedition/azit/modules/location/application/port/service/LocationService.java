package com.youthexpedition.azit.modules.location.application.port.service;

import com.youthexpedition.azit.modules.location.application.port.in.LocationUseCase;
import com.youthexpedition.azit.modules.location.application.port.in.dto.LocationSearchResponse;
import com.youthexpedition.azit.modules.location.application.port.out.LoadLocationPort;
import com.youthexpedition.azit.modules.location.application.port.service.mapper.LocationResponseMapper;
import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService implements LocationUseCase {
    private final LoadLocationPort loadLocationPort;
    private final LocationResponseMapper locationResponseMapper;

    @Override
    public List<LocationSearchResponse> searchLocation(String query) {
        List<LocationSearchResult> locationSearchResults = loadLocationPort.searchByKeyword(query);
        return locationResponseMapper.toSearchResponseList(locationSearchResults);
    }
}
