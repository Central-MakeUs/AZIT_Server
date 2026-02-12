package com.youthexpedition.azit.infrastructure.common.resolver;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentAccessTokenArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @CurrentAccessToken 일 때만 동작
        return parameter.hasParameterAnnotation(CurrentAccessToken.class) &&
                parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        // JwtProvider에서 UsernamePasswordAuthenticationToken 생성 시 두 번째 인자(credentials)로 토큰을 넣어주었으므로 이를 꺼냄
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }
        return authentication.getCredentials();
    }
}
