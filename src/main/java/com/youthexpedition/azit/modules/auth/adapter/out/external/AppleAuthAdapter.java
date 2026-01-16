package com.youthexpedition.azit.modules.auth.adapter.out.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthexpedition.azit.infrastructure.auth.util.AppleJwtUtils;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.ApplePublicKeyResponse;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.AppleUserInfoResponse;
import com.youthexpedition.azit.modules.auth.adapter.out.external.Feign.AppleFeignClient;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.AppleTokenResponse;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAuthAdapter implements SocialAuthPort {

    private final AppleFeignClient appleFeignClient;
    private final AppleJwtUtils appleJwtUtils;
    private final ObjectMapper objectMapper;

    @Value("${oauth.apple.client-id}")
    private String clientId;
    @Value("${oauth.apple.redirect-uri}")
    private String redirectUri;

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    @Override
    public SocialProfile getSocialProfile(SocialLoginCommand command) {
        // Apple 공개키 목록 조회
        ApplePublicKeyResponse keys = appleFeignClient.getApplePublicKeys();

        // id_token 헤더에서 kid, alg 추출
        String kid = appleJwtUtils.getKidFromHeader(command.idToken());

        // 일치하는 공개키 찾기
        ApplePublicKeyResponse.ApplePublicKey matchedKey = keys.getMatchedKey(kid, "RS256");

        // ID Token 서명 검증 및 정보 추출
        Claims claims = appleJwtUtils.verifyIdToken(command.idToken(), matchedKey);

        String nickname = parseNickname(command.user());
        String email = claims.get("email", String.class);

        // 인가 코드로 리프레시 토큰 요청
        String appleRefreshToken = fetchAppleRefreshToken(command.authorizationCode());

        // 소셜 프로필 생성
        return SocialProfile.builder()
                .socialProvider(SocialProvider.APPLE)
                .socialProviderId(claims.getSubject()) // sub 값
                .email(email)
                .nickname(nickname)
                .refreshToken(appleRefreshToken)
                .build();
    }

    /**
     * Apple이 보낸 user JSON 문자열에서 이름을 추출하여 닉네임 생성
     */
    private String parseNickname(String userJson) {
        // user 정보가 없는 경우 (재로그인 시)
        if (userJson == null || userJson.isBlank()) {
            log.info("user 정보 없음, 재로그인한 유저");
            return "Apple User";
        }

        try {
            AppleUserInfoResponse.User user = objectMapper.readValue(userJson, AppleUserInfoResponse.User.class);

            if (user.name() != null) {
                String lastName = user.name().lastName() != null ? user.name().lastName() : "";
                String firstName = user.name().firstName() != null ? user.name().firstName() : "";
                return (lastName + firstName).trim();
            }
        } catch (JsonProcessingException e) {
            // 파싱 실패 시 기본값 반환
            return "Apple User";
        }

        return "Apple User";
    }

    private String fetchAppleRefreshToken(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            return null;
        }

        String clientSecret = appleJwtUtils.createClientSecret();
        AppleTokenResponse tokenResponse = appleFeignClient.getToken(
                clientId, clientSecret, authorizationCode, GRANT_TYPE_AUTHORIZATION_CODE, redirectUri);

        return tokenResponse.refreshToken();
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }
}
