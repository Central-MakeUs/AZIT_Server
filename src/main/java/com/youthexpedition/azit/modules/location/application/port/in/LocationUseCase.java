package com.youthexpedition.azit.modules.location.application.port.in;

import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;

import java.util.List;

public interface LocationUseCase {
    List<LocationSearchResult> searchLocation(String query);
}
