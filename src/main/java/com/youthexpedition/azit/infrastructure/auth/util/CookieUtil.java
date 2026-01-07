package com.youthexpedition.azit.infrastructure.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${jwt.cookie.secure}")
    private boolean secure;

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
                .sameSite("Strict") // CSRF 방어 강화
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
}