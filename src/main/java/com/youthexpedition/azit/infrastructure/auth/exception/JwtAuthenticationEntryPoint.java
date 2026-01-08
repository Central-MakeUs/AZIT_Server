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

        // 응답 헤더 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized

        CommonErrorResponse errorResponse = CommonErrorResponse.of(AuthErrorCode.UNAUTHORIZED);

        // JSON으로 변환하여 응답 바디에 쓰기
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}