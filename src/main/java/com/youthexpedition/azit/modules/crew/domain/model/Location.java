package com.youthexpedition.azit.modules.crew.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Location {
    private final String name;      // 장소 명칭
    private final String address;   // 상세 주소
    private final String detailedLocation; // 세부 장소
    private final Double latitude;  // 위도
    private final Double longitude; // 경도
}
