package com.youthexpedition.azit.infrastructure.auth.jwt;

import com.youthexpedition.azit.infrastructure.auth.service.MemberDetailsService;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;
    private final SecretKey secretKey;

    @Getter
    private final String refreshTokenName;

    private final MemberDetailsService memberDetailsService;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKeyPlain,
            @Value("${jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds,
            @Value("${jwt.refresh-token-name}") String refreshTokenName,
            MemberDetailsService memberDetailsService
    ) {
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        this.refreshTokenName = refreshTokenName;
        this.memberDetailsService = memberDetailsService;
        this.secretKey = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Long memberId, MemberRole role) {
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .subject(memberId.toString())
                .claim("role", "ROLE_" + role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpirationSeconds * 1000);

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 인증 객체(Authentication) 추출
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 Member ID 추출
        Long memberId = extractMemberId(token);
        UserDetails userDetails = memberDetailsService.loadUserByUsername(memberId.toString());

        // 인증 객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public Long extractMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}