package com.youthexpedition.azit.infrastructure.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthexpedition.azit.infrastructure.common.response.CommonErrorResponse;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 필터에서 넘겨준 커스텀 에러 코드가 있는지 확인
        AuthErrorCode errorCode = (AuthErrorCode) request.getAttribute("exception");

        // 별도 설정된 에러가 없다면 기본 UNAUTHORIZED 사용
        if (errorCode == null) {
            errorCode = AuthErrorCode.UNAUTHORIZED;
        }

        // 응답 헤더 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(errorCode.getStatus().value());

        CommonErrorResponse errorResponse = CommonErrorResponse.of(errorCode);

        // JSON으로 변환하여 응답 바디에 쓰기
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}