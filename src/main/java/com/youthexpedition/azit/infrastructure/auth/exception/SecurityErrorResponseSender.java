package com.youthexpedition.azit.infrastructure.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthexpedition.azit.infrastructure.common.response.CommonErrorResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SecurityErrorResponseSender {

    private final ObjectMapper objectMapper = new ObjectMapper(); // 공용으로 사용

    public void send(HttpServletRequest request, HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
        log.warn("[Security Error] URI: {} {}, ErrorCode: {}, Message: {}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), errorCode.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(errorCode.getStatus().value());

        CommonErrorResponse errorResponse = CommonErrorResponse.of(errorCode);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}