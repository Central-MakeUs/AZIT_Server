package com.youthexpedition.azit.modules.location.application.port.service;

import com.youthexpedition.azit.modules.location.application.port.in.LocationUseCase;
import com.youthexpedition.azit.modules.location.application.port.out.LoadLocationPort;
import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService implements LocationUseCase {
    private final LoadLocationPort loadLocationPort;

    @Override
    public List<LocationSearchResult> searchLocation(String query) {
        return loadLocationPort.searchByKeyword(query);
    }
}
