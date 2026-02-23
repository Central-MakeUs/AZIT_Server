package com.youthexpedition.azit.modules.location.adapter.out.external;

import com.youthexpedition.azit.modules.location.adapter.out.external.dto.NaverLocalSearchResponse;
import com.youthexpedition.azit.modules.location.adapter.out.external.feign.NaverSearchFeignClient;
import com.youthexpedition.azit.modules.location.application.port.out.LoadLocationPort;
import com.youthexpedition.azit.modules.location.domain.model.LocationSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverLocationAdapter implements LoadLocationPort {

    private final NaverSearchFeignClient naverSearchFeignClient;

    @Value("${naver.search.client-id}")
    private String clientId;
    @Value("${naver.search.client-secret}")
    private String clientSecret;

    private static final int MAX_DISPLAY_COUNT = 5;
    private static final String SEARCH_SORT_TYPE = "random";
    private static final String TITLE_TAG_REMOVAL_REGEX = "<(/)?b>";
    private static final double NAVER_COORDINATE_PRECISION = 10000000.0; // 좌표 정밀도 변환값

    @Override
    public List<LocationSearchResult> searchLocation(String query) {
        // 네이버 지역 검색 API 호출
        NaverLocalSearchResponse response = naverSearchFeignClient.searchLocal(
                clientId, clientSecret, query, MAX_DISPLAY_COUNT, SEARCH_SORT_TYPE);

        return response.items().stream()
                .map(item -> LocationSearchResult.of(
                        item.title().replaceAll(TITLE_TAG_REMOVAL_REGEX, ""), // <b> 태그 제거
                        item.category(),
                        item.roadAddress().isBlank() ? item.address() : item.roadAddress(),
                        Double.parseDouble(item.mapy()) / NAVER_COORDINATE_PRECISION,
                        Double.parseDouble(item.mapx()) / NAVER_COORDINATE_PRECISION
                ))
                .toList();
    }
}
