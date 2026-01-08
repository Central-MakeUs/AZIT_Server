package com.youthexpedition.azit.modules.auth.adapter.in.web;

import com.youthexpedition.azit.infrastructure.auth.util.CookieUtil;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.auth.adapter.in.web.docs.AuthControllerDocs;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginRequest;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginResponse;
import com.youthexpedition.azit.modules.auth.application.port.in.SocialLoginUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final SocialLoginUseCase socialLoginUseCase;
    private final CookieUtil cookieUtil;

    @PostMapping("/social-login/{provider}")
    public CommonResponse<SocialLoginResponse> socialLogin(@PathVariable SocialProvider provider,
                                                           @Valid @RequestBody SocialLoginRequest request, HttpServletResponse response) {
        SocialLoginCommand command = request.toCommand(provider);
        AuthToken authToken = socialLoginUseCase.login(command);
        SocialLoginResponse loginResponse = SocialLoginResponse.from(authToken);

        cookieUtil.setRefreshTokenCookie(response, authToken.refreshToken());

        return CommonResponse.of(CommonSuccessCode.SUCCESS, loginResponse);
    }
}
