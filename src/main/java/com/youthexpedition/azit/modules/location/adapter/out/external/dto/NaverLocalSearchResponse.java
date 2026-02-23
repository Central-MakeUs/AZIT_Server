package com.youthexpedition.azit.modules.location.adapter.out.external.dto;

import java.util.List;

public record NaverLocalSearchResponse(
        String lastBuildDate,
        Integer total,
        Integer start,
        Integer display,
        List<Item> items
) {
    public record Item(
            String title,       // 업체/기관 명칭
            String link,        // 상세 정보 URL
            String category,    // 분류 정보
            String description, // 상세 설명
            String address,     // 지번 주소
            String roadAddress, // 도로명 주소
            String mapx,        // X 좌표 (WGS84 기준)
            String mapy         // Y 좌표 (WGS84 기준)
    ) {}
}
