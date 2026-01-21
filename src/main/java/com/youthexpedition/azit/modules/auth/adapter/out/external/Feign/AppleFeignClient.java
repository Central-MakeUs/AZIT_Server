package com.youthexpedition.azit.modules.auth.adapter.out.external.Feign;

import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.ApplePublicKeyResponse;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.AppleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "AppleFeignClient", url = "${oauth.apple-url}")
public interface AppleFeignClient {

    @PostMapping(value = "/auth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AppleTokenResponse getToken(
            @RequestParam("client_id") String clientId,         // Service ID
            @RequestParam("client_secret") String clientSecret, // 생성한 JWT
            @RequestParam("code") String code,                 // 인가 코드
            @RequestParam("grant_type") String grantType,       // "authorization_code"
            @RequestParam(value = "redirect_uri", required = false) String redirectUri
    );

    // 공개키 목록 조회
    @GetMapping("/auth/keys")
    ApplePublicKeyResponse getApplePublicKeys();

    // 연동 해제
    @PostMapping(value = "/auth/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void revoke(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("token") String token,
            @RequestParam("token_type_hint") String tokenTypeHint // "refresh_token"
    );
}
