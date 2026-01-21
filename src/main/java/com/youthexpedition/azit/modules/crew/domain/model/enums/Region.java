package com.youthexpedition.azit.modules.crew.domain.model.enums;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Region {
    SEOUL("서울"),
    GYEONGGI_INCHEON("경기/인천"),
    CHUNGCHEONG_DAEJEON("충청/대전"),
    JEOLLA_GWANGJU("전라/광주"),
    GYEONGBUK_DAEGU("경북/대구"),
    GYEONGNAM_BUSAN("경남/부산"),
    GANGWON("강원"),
    JEJU("제주");

    private final String description;

    public static Region of(String value) {
        return Arrays.stream(Region.values())
                .filter(r -> r.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CrewErrorCode.INVALID_REGION));
    }
}
