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

    @Override
    public List<LocationSearchResult> searchByKeyword(String keyword) {
        // 네이버 지역 검색 API 호출
        NaverLocalSearchResponse response = naverSearchFeignClient.searchLocal(
                clientId, clientSecret, keyword, 10, "random");

        return response.items().stream()
                .map(item -> LocationSearchResult.of(
                        item.title().replaceAll("<(/)?b>", ""), // <b> 태그 제거
                        item.category(),
                        item.roadAddress().isBlank() ? item.address() : item.roadAddress(),
                        Double.parseDouble(item.mapy()) / 10000000.0,
                        Double.parseDouble(item.mapx()) / 10000000.0
                ))
                .toList();
    }
}
