package com.youthexpedition.azit.modules.location.application.port.out;

import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;

import java.util.List;

public interface LoadLocationPort {
    List<LocationSearchResult> searchLocation(String query);
}
