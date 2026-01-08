package com.youthexpedition.azit.modules.auth.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoAuthAdapter {

    private final KakaoAuthFeignClient kakaoAuthFeignClient;
    private final KakaoApiFeignClient kakaoApiFeignClient;

    @Value("${oauth.kakao.client-id}") private String clientId;
    @Value("${oauth.kakao.redirect-uri}") private String redirectUri;
    @Value("${oauth.kakao.client-secret}") private String clientSecret;


}
