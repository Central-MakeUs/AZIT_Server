package com.youthexpedition.azit.infrastructure.auth.util;

import com.youthexpedition.azit.infrastructure.auth.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    private final boolean secure;
    private final JwtProvider jwtProvider;

    public CookieUtil(JwtProvider jwtProvider, @Value("${jwt.cookie.secure}") boolean secure) {
        this.jwtProvider = jwtProvider;
        this.secure = secure;
    }

    /**
     * 요청에서 특정 이름의 쿠키를 반환
     */
    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * 쿠키 생성
     */
    public ResponseCookie createCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(secure) // 운영 환경에서는 HTTPS가 필수이므로 true 설정
                .sameSite("None") // CSRF 방어 강화
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * 만료된 쿠키 생성
     */
    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .maxAge(0)
                .build();
    }

    /**
     * 리프레시 토큰 쿠키 세팅
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createCookie(
                jwtProvider.getRefreshTokenName(),
                refreshToken,
                jwtProvider.getRefreshTokenExpirationSeconds()
        );
        response.addHeader("Set-Cookie", cookie.toString());
    }
}