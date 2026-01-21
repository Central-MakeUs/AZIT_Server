package com.youthexpedition.azit.infrastructure.auth.exception;

import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 401 Unauthorized 인증 실패 핸들러
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseSender responseSender;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 필터에서 넘겨준 커스텀 에러 코드가 있는지 확인
        BaseErrorCode errorCode = (BaseErrorCode) request.getAttribute("exception");

        // 별도 설정된 에러가 없다면 기본 UNAUTHORIZED 사용
        if (errorCode == null) {
            errorCode = AuthErrorCode.UNAUTHORIZED;
        }

        log.warn("인증 실패, Message: {}", authException.getMessage());
        responseSender.send(request, response, errorCode);
    }
}