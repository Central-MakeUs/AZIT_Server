package com.youthexpedition.azit.modules.auth.adapter.out.external;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoTokenResponse;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoUserInfoResponse;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthAdapter implements SocialAuthPort {

    private final KakaoAuthFeignClient kakaoAuthFeignClient;
    private final KakaoApiFeignClient kakaoApiFeignClient;

    @Value("${oauth.kakao.client-id}")
    private String clientId;
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Override
    public SocialProfile getSocialProfile(String authorizationCode) {
        try {
            // 카카오 토큰 요청
            KakaoTokenResponse tokenResponse = kakaoAuthFeignClient.getToken(
                    "authorization_code", clientId, redirectUri, authorizationCode, clientSecret
            );

            // 카카오 사용자 정보 요청
            KakaoUserInfoResponse userInfo = kakaoApiFeignClient.getUserInfo("Bearer " + tokenResponse.accessToken());

            var account = userInfo.kakaoAccount();
            var profile = account.profile();

            String profileImageUrl = (profile.isDefaultImage() != null && profile.isDefaultImage()) ? null : profile.profileImageUrl();
            String email = (account.hasEmail() != null && account.hasEmail()) ? account.email() : null;

            return SocialProfile.builder()
                    .socialProviderId(userInfo.id().toString())
                    .socialProvider(SocialProvider.KAKAO)
                    .nickname(profile.nickname())
                    .email(email)
                    .profileImageUrl(profileImageUrl)
                    .build();
        } catch (FeignException.BadRequest e) {
            log.error("카카오 인가 코드 검증 실패: {}", e.contentUTF8());
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_CODE);
        } catch (FeignException e) {
            // 기타 통신 오류
            log.error("카카오 API 호출 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.SOCIAL_AUTHENTICATION_FAILED);
        }
    }
}
