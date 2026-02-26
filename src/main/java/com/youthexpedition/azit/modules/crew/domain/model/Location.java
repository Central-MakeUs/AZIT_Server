package com.youthexpedition.azit.modules.crew.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Location {
    private final String placeName;      // 집합 장소명
    private final String address;   // 주소
    private final String meetingSpot; // 모이는 지점
    private final Double latitude;  // 위도
    private final Double longitude; // 경도
}
