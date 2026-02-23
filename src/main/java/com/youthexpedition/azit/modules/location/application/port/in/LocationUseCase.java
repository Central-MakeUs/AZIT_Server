package com.youthexpedition.azit.modules.location.application.port.in;

import com.youthexpedition.azit.modules.location.application.port.in.dto.LocationSearchResponse;

import java.util.List;

public interface LocationUseCase {
    List<LocationSearchResponse> searchLocation(String query);
}
