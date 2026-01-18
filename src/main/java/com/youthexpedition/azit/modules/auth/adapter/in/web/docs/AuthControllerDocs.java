package com.youthexpedition.azit.modules.auth.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginRequest;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Tag(name = "Auth" , description = "사용자 인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "소셜 로그인(애플 제외)", description = "인가 코드를 통해 소셜 로그인을 진행하고 JWT 토큰과 멤버 상태를 반환합니다.")
    @ApiErrorCodeExamples({
            "INVALID_SOCIAL_CODE", "SOCIAL_AUTHENTICATION_FAILED"
    })
    CommonResponse<SocialLoginResponse> socialLogin(@PathVariable SocialProvider provider, @Valid @RequestBody SocialLoginRequest request, HttpServletResponse response);

    @Operation(summary = "애플 소셜 로그인(백엔드용)", description = "애플 전용 로그인 콜백 엔드포인트입니다. id_token을 검증하여 로그인을 처리합니다."
    )
    @ApiErrorCodeExamples({
            "INVALID_APPLE_ID_TOKEN", "APPLE_PUBLIC_KEY_NOT_FOUND", "APPLE_CLIENT_SECRET_CREATION_FAILED"
    })
    void appleLogin(@RequestParam("code") String code, @RequestParam("id_token") String idToken,
                    @RequestParam(value = "user", required = false) String user, HttpServletResponse response) throws IOException;

    @Operation(summary = "토큰 재발급", description = "쿠키에 저장된 Refresh Token을 사용하여 Access Token을 갱신합니다. (RTR 방식 적용)")
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<SocialLoginResponse> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "로그아웃", description = """
            현재 사용자의 세션을 종료하고 리프레시 토큰 쿠키를 제거합니다. <br>
            accessToken 파라미터는 무시하시고 기존대로 헤더에 액세스 토큰 넣어서 요청 보내시면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> logout(@CurrentMemberId Long memberId, @CurrentAccessToken String accessToken, HttpServletResponse response);
}
