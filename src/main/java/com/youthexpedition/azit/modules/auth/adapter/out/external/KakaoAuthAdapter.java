package com.youthexpedition.azit.modules.auth.adapter.out.external;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.adapter.out.external.Feign.KakaoApiFeignClient;
import com.youthexpedition.azit.modules.auth.adapter.out.external.Feign.KakaoAuthFeignClient;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoTokenResponse;
import com.youthexpedition.azit.modules.auth.adapter.out.external.dto.KakaoUserInfoResponse;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
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
    @Value("${oauth.kakao.redirect-url}")
    private String redirectUri;
    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;
    @Value("${oauth.kakao.admin-key}")
    private String adminKey;

    private static final String TARGET_ID_TYPE = "user_id";
    private static final String AUTHORIZATION_HEADER = "KakaoAK ";

    @Override
    public SocialProfile getSocialProfile(SocialLoginCommand command) {
        try {
            // 카카오 토큰 요청
            KakaoTokenResponse tokenResponse = kakaoAuthFeignClient.getToken(
                    "authorization_code", clientId, redirectUri, command.authorizationCode(), clientSecret
            );

            // 카카오 사용자 정보 요청
            KakaoUserInfoResponse userInfo = kakaoApiFeignClient.getUserInfo("Bearer " + tokenResponse.accessToken());

            var account = userInfo.kakaoAccount();
            var profile = account.profile();

            String profileImageUrl = Boolean.TRUE.equals(profile.isDefaultImage()) ? null : profile.profileImageUrl();
            String email = Boolean.TRUE.equals(account.hasEmail()) ? account.email() : null;
            // 이메일 값이 존재하면 동의(true), null이면 미동의(false)
            boolean isEmailSharingEnabled = (email != null);

            return SocialProfile.builder()
                    .socialProviderId(userInfo.id().toString())
                    .socialProvider(SocialProvider.KAKAO)
                    .nickname(profile.nickname())
                    .email(email)
                    .isEmailSharingEnabled(isEmailSharingEnabled)
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

    @Override
    public void revoke(SocialRevokeCommand command) {
        String socialProviderId = command.socialProviderId();
        if (socialProviderId == null || socialProviderId.isBlank()) {
            log.warn("카카오 연동 해제를 위한 provider ID가 없습니다.");
            return;
        }

        try {
            kakaoApiFeignClient.unlink(AUTHORIZATION_HEADER + adminKey, TARGET_ID_TYPE, Long.parseLong(socialProviderId));
            log.info("카카오 연동 해제 성공");
        } catch (Exception e) {
            log.error("카카오 연동 해제 실패: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.KAKAO_REVOKE_FAILED);
        }
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }
}
