package com.youthexpedition.azit.modules.location.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.location.adapter.in.web.docs.LocationControllerDocs;
import com.youthexpedition.azit.modules.location.adapter.in.web.dto.LocationSearchResponse;
import com.youthexpedition.azit.modules.location.application.port.in.LocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController implements LocationControllerDocs {

    private final LocationUseCase locationUseCase;

    @GetMapping("/search")
    public CommonResponse<List<LocationSearchResponse>> search(@RequestParam String query) {
        List<LocationSearchResponse> response = locationUseCase.searchLocation(query).stream()
                .map(result -> new LocationSearchResponse(
                        result.placeName(),
                        result.category(),
                        result.address(),
                        result.latitude(),
                        result.longitude()
                ))
                .toList();

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
