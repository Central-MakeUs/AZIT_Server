package com.youthexpedition.azit.modules.auth.adapter.in.web;

import com.youthexpedition.azit.infrastructure.auth.util.CookieUtil;
import com.youthexpedition.azit.infrastructure.auth.util.TokenUtil;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.auth.adapter.in.web.docs.AuthControllerDocs;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginRequest;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginResponse;
import com.youthexpedition.azit.modules.auth.application.port.in.SocialLoginUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.TokenUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final SocialLoginUseCase socialLoginUseCase;
    private final TokenUseCase tokenUseCase;
    private final CookieUtil cookieUtil;

    @Value("${oauth.apple.success-redirect-uri}")
    private String appleSuccessRedirectUri;

    @PostMapping("/social-login/{provider}")
    public CommonResponse<SocialLoginResponse> socialLogin(@PathVariable SocialProvider provider,
                                                           @Valid @RequestBody SocialLoginRequest request, HttpServletResponse response) {
        SocialLoginCommand command = request.toCommand(provider);
        AuthResult authResult = socialLoginUseCase.login(command);
        SocialLoginResponse loginResponse = SocialLoginResponse.from(authResult);

        cookieUtil.setRefreshTokenCookie(response, authResult.authToken().refreshToken());

        return CommonResponse.of(CommonSuccessCode.SUCCESS, loginResponse);
    }

    // 애플 로그인 전용
    @PostMapping(value = "/social-login/apple", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void appleLogin(@RequestParam("code") String code, @RequestParam("id_token") String idToken,
                           @RequestParam(value = "user", required = false) String user, HttpServletResponse response) throws IOException {
        SocialLoginCommand command = SocialLoginCommand.of(SocialProvider.APPLE, code, idToken, user);
        AuthResult authResult = socialLoginUseCase.login(command);

        cookieUtil.setRefreshTokenCookie(response, authResult.authToken().refreshToken());

        // 프론트 페이지로 리다이렉트
        response.sendRedirect(appleSuccessRedirectUri);
    }

    @PostMapping("/reissue")
    public CommonResponse<SocialLoginResponse> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshToken(request);
        AuthResult authResult = tokenUseCase.reissue(refreshToken);
        SocialLoginResponse loginResponse = SocialLoginResponse.from(authResult);

        cookieUtil.setRefreshTokenCookie(response, authResult.authToken().refreshToken());

        return CommonResponse.of(CommonSuccessCode.SUCCESS, loginResponse);
    }

    @PostMapping("/logout")
    public CommonResponse<Void> logout(@CurrentMemberId Long memberId, HttpServletResponse response,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = TokenUtil.extractToken(authorizationHeader);
        tokenUseCase.logout(memberId, accessToken);
        cookieUtil.deleteRefreshTokenCookie(response);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
