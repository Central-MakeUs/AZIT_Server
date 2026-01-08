package com.youthexpedition.azit.modules.auth.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginRequest;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.SocialLoginResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
