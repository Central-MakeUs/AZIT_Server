package com.youthexpedition.azit.modules.auth.adapter.in.web.docs;

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

@Tag(name = "Auth" , description = "사용자 인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "소셜 로그인", description = "인가 코드를 통해 소셜 로그인을 진행하고 JWT 토큰을 발급합니다.")
    @ApiErrorCodeExamples({
            "INVALID_SOCIAL_CODE", "SOCIAL_AUTHENTICATION_FAILED"
    })
    CommonResponse<SocialLoginResponse> socialLogin(@PathVariable SocialProvider provider, @Valid @RequestBody SocialLoginRequest request, HttpServletResponse response);

    @Operation(summary = "토큰 재발급", description = "쿠키에 저장된 Refresh Token을 사용하여 Access Token을 갱신합니다. (RTR 방식 적용)")
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED"
    })
    CommonResponse<SocialLoginResponse> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "현재 사용자의 세션을 종료하고 리프레시 토큰 쿠키를 제거합니다.")
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED"
    })
    CommonResponse<Void> logout(@CurrentMemberId Long memberId, HttpServletResponse response);
}
