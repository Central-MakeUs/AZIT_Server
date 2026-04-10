package com.youthexpedition.azit.modules.auth.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.AppleNotificationRequest;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginRequest;
import com.youthexpedition.azit.modules.auth.application.port.in.dto.SocialLoginResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(
            summary = "소셜 로그인 (애플 제외)",
            description = """
            카카오 등 소셜 플랫폼의 인가 코드 및 액세스 토큰을 통해 로그인을 진행하고 JWT 토큰과 회원의 현재 상태, 최근 가입한 크루 ID를 반환합니다. <br><br>
            
            **[참고 사항]** <br>
            * 보안을 위해 리프레시 토큰은 HttpOnly 쿠키에 저장되어 발급됩니다.
            * 응답으로 받은 **status** 값에 따라 앱의 초기 진입 화면(약관 동의, 온보딩, 메인 등)을 결정해야 합니다. <br>
            """
    )
    @ApiErrorCodeExamples({
            "INVALID_SOCIAL_CODE", "SOCIAL_AUTHENTICATION_FAILED", "INVALID_KAKAO_ACCESS_TOKEN", "MISSING_SOCIAL_CREDENTIAL"
    })
    CommonResponse<SocialLoginResponse> socialLogin(
            @PathVariable SocialProvider provider, @Valid @RequestBody SocialLoginRequest request, HttpServletResponse response);

    @Operation(
            summary = "애플 소셜 로그인 (백엔드 전용)",
            description = """
            애플 서버로부터 직접 리다이렉트되는 콜백 엔드포인트입니다. 클라이언트가 아닌 서버 간 통신을 통해 로그인을 처리합니다. <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "INVALID_APPLE_ID_TOKEN", "APPLE_PUBLIC_KEY_NOT_FOUND", "APPLE_CLIENT_SECRET_CREATION_FAILED"
    })
    void appleLogin(@RequestParam("code") String code, @RequestParam("id_token") String idToken,
                    @RequestParam(value = "user", required = false) String user,
                    @RequestParam(value = "state", required = false) String state, HttpServletResponse response) throws IOException;

    @Operation(
            summary = "토큰 재발급",
            description = """
            쿠키에 저장된 리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다. <br><br>
            
            **[RTR(Refresh Token Rotation) 방식 적용]** <br>
            * 토큰 재발급 시 기존 리프레시 토큰은 폐기되고 새로운 리프레시 토큰이 쿠키에 다시 저장됩니다. <br>
            * 만약 이미 사용된 리프레시 토큰이 다시 제출될 경우, 보안 위협으로 간주하여 쿠키 삭제 및 로그아웃됩니다. (TOKEN_REUSE_DETECTED)
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<SocialLoginResponse> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(
            summary = "로그아웃",
            description = """
            현재 사용자의 세션을 종료하고 리프레시 토큰 쿠키를 제거합니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> logout(
            @Parameter(hidden = true) @CurrentMemberId Long memberId, @Parameter(hidden = true) @CurrentAccessToken String accessToken,
            HttpServletResponse response);

    @Operation(
            summary = "애플 서버 알림 수신 (S2S)",
            description = """
            Apple 서버가 사용자 상태 변경 알림(계정 연동 해제 등)을 보낼 때 이를 수신하여 서버 데이터를 동기화합니다. <br><br>
            
            **[수신 케이스]** <br>
            * CONSENT_REVOKED: 사용자가 Apple 설정에서 앱 연동을 해제한 경우 탈퇴 처리를 진행합니다. <br>
            * ACCOUNT_DELETE: Apple 계정이 삭제된 경우 탈퇴 처리를 진행합니다.
            * EMAIL_ENABLED: 사용자가 Apple 설정에서 이메일 공유를 활성화한 경우(숨기기 해제) 해당 플래그를 Y로 설정합니다.
            * EMAIL_DISABLED: 사용자가 Apple 설정에서 이메일 공유를 비활성화한 경우(숨기기 설정) 해당 플래그를 N으로 설정합니다.
            """
    )
    @ApiErrorCodeExamples({
            "INVALID_APPLE_ID_TOKEN", "APPLE_PUBLIC_KEY_NOT_FOUND"
    })
    CommonResponse<Void> receiveAppleNotification(@Valid @RequestBody AppleNotificationRequest request);
}
