package com.youthexpedition.azit.infrastructure.auth.jwt;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 accessToken 추출
        String accessToken = resolveToken(request);

        // accessToken 유효성 검증 및 인증 처리
        try {
            // 토큰이 있고 유효한지 검증
            if (StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BusinessException e) {
            // 만료(EXPIRED_TOKEN) 또는 유효하지 않음(INVALID_TOKEN) 예외를 request에 저장
            // 이후 AuthenticationEntryPoint에서 값을 꺼내 처리
            request.setAttribute("exception", e.getErrorCode());
        } catch (Exception e) {
        // NPE 등 기타 예외 발생 시 로그를 남기고 유효하지 않은 토큰으로 처리
        log.error("Authentication failed: ", e);
        request.setAttribute("exception", AuthErrorCode.INVALID_TOKEN);
    }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열만 반환
        }
        return null;
    }
}