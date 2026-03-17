package com.youthexpedition.azit.modules.auth.adapter.out.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthexpedition.azit.infrastructure.auth.util.AppleJwtUtils;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.ApplePublicKeyResponse;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.AppleUserInfoResponse;
import com.youthexpedition.azit.modules.auth.adapter.out.external.feign.AppleFeignClient;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.AppleNotificationPayload;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.AppleTokenResponse;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
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

    @Value("${apple.oauth.client-id}")
    private String clientId;
    @Value("${apple.oauth.redirect-url}")
    private String redirectUrl;

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String TOKEN_TYPE_HINT = "refresh_token";
    private static final String APPLE_KEY_ALGORITHM = "RS256";
    private static final String APPLE_USER_NAME = "Apple User";

    @Override
    public SocialProfile getSocialProfile(SocialLoginCommand command) {
        // Apple 공개키 목록 조회
        ApplePublicKeyResponse keys = appleFeignClient.getApplePublicKeys();

        // id_token 헤더에서 kid, alg 추출
        String kid = appleJwtUtils.getKidFromHeader(command.idToken());

        // 일치하는 공개키 찾기
        ApplePublicKeyResponse.ApplePublicKey matchedKey = keys.getMatchedKey(kid, APPLE_KEY_ALGORITHM);

        // ID Token 서명 검증 및 정보 추출
        Claims claims = appleJwtUtils.verifyIdToken(command.idToken(), matchedKey);

        String nickname = parseNickname(command.user());
        String email = claims.get("email", String.class);
        // 이메일 정보가 존재한다면 사용을 허용한 것으로 간주
        boolean isEmailSharingEnabled = (email != null);

        // 인가 코드로 리프레시 토큰 요청
        String refreshToken = fetchRefreshToken(command.authorizationCode());

        // 소셜 프로필 생성
        return SocialProfile.builder()
                .socialProvider(SocialProvider.APPLE)
                .socialProviderId(claims.getSubject()) // sub 값
                .email(email)
                .isEmailSharingEnabled(isEmailSharingEnabled)
                .nickname(nickname)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Apple이 보낸 user JSON 문자열에서 이름을 추출하여 닉네임 생성
     */
    private String parseNickname(String userJson) {
        // user 정보가 없는 경우 (재로그인 시)
        if (userJson == null || userJson.isBlank()) {
            log.debug("apple 계정으로 재로그인한 유저입니다(유저 정보 없음).");
            return APPLE_USER_NAME;
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
            return APPLE_USER_NAME;
        }

        return APPLE_USER_NAME;
    }

    private String fetchRefreshToken(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            return null;
        }

        String clientSecret = appleJwtUtils.createClientSecret();
        AppleTokenResponse tokenResponse = appleFeignClient.getToken(
                clientId, clientSecret, authorizationCode, GRANT_TYPE_AUTHORIZATION_CODE, redirectUrl);

        return tokenResponse.refreshToken();
    }

    @Override
    public void revoke(SocialRevokeCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("애플 연동 해제를 위한 리프레시 토큰이 없습니다.");
            return;
        }

        // client_secret 생성
        String clientSecret = appleJwtUtils.createClientSecret();
        try {
            // 애플 서버에 연동 해제 요청
            appleFeignClient.revoke(clientId, clientSecret, refreshToken, TOKEN_TYPE_HINT);
            log.info("애플 연동 해제에 성공했습니다.");
        } catch (Exception e) {
            log.error("애플 연동 해제에 실패했습니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.APPLE_REVOKE_FAILED);
        }
    }

    public AppleNotificationPayload.Event parseNotification(String payload) {
        try {
            // 헤더에서 kid 추출 및 공개키 매칭
            String kid = appleJwtUtils.getKidFromHeader(payload);
            ApplePublicKeyResponse keys = appleFeignClient.getApplePublicKeys();
            ApplePublicKeyResponse.ApplePublicKey matchedKey = keys.getMatchedKey(kid, APPLE_KEY_ALGORITHM);

            // 서명 검증 및 페이로드 추출
            Claims claims = appleJwtUtils.verifyIdToken(payload, matchedKey);

            // events 클레임 파싱
            Object eventsObj = claims.get("events");
            String eventsJson;

            // 타입에 따라 처리
            if (eventsObj instanceof String) {
                // 이미 문자열이라면 그대로 사용 (Apple S2S v2 기본값)
                eventsJson = (String) eventsObj;
            } else {
                // 객체 형태일 경우 JSON 문자열로 변환
                eventsJson = objectMapper.writeValueAsString(eventsObj);
            }

            return objectMapper.readValue(eventsJson, AppleNotificationPayload.Event.class);

        } catch (Exception e) {
            log.error("애플 알림 내용 파싱에 실패했습니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.INVALID_APPLE_ID_TOKEN);
        }
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }
}
