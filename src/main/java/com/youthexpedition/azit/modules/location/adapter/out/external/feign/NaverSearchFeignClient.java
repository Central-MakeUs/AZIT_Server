package com.youthexpedition.azit.modules.location.adapter.out.external.feign;

import com.youthexpedition.azit.modules.location.adapter.out.external.dto.NaverLocalSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NaverSearchFeignClient", url = "${naver.search.api-url}")
public interface NaverSearchFeignClient {

    // 장소 검색
    @GetMapping("/v1/search/local.json")
    NaverLocalSearchResponse searchLocal(
            @RequestHeader("X-Naver-Client-Id") String clientId,
            @RequestHeader("X-Naver-Client-Secret") String clientSecret,
            @RequestParam("query") String query,
            @RequestParam("display") Integer display,
            @RequestParam("sort") String sort
    );
}
