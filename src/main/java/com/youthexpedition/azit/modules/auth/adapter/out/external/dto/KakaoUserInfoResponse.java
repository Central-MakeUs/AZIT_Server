package com.youthexpedition.azit.modules.auth.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoResponse(
        Long id, // providerID
        KakaoAccount kakaoAccount
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoAccount(
            KakaoProfile profile,
            String email,
            Boolean hasEmail // 이메일 존재 여부
    ) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoProfile(
            String nickname, // 필수 동의 항목
            String profileImageUrl, // 선택 동의 항목
            Boolean isDefaultImage // 기본 이미지 여부
    ) {}
}
