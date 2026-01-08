package com.youthexpedition.azit.modules.auth.adapter.out.external;

import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoAuthClient", url = "${oauth.kakao.api-url}")
public interface KakaoApiFeignClient {

    @GetMapping("/v2/user/me")
    KakaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
