package com.youthexpedition.azit.modules.auth.adapter.out.external.feign;

import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "KakaoApiFeignClient", url = "${kakao.oauth.api-url}")
public interface KakaoApiFeignClient {

    // 회원 정보 조회
    @GetMapping("/v2/user/me")
    KakaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);

    // 연동 해제
    @PostMapping(value = "/v1/user/unlink", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void unlink(
            @RequestHeader("Authorization") String adminKey, // KakaoAK
            @RequestParam("target_id_type") String targetIdType, // "user_id"
            @RequestParam("target_id") Long targetId // 카카오 provider ID
    );
}
