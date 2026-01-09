package com.youthexpedition.azit.infrastructure.auth.jwt;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Getter
    private final long accessTokenExpirationSeconds;

    @Getter
    private final long refreshTokenExpirationSeconds;

    @Getter
    private final String refreshTokenName;

    private final SecretKey secretKey;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKeyPlain,
            @Value("${jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds,
            @Value("${jwt.refresh-token-name}") String refreshTokenName
    ) {
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        this.refreshTokenName = refreshTokenName;
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
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(refreshTokenExpirationSeconds);

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 인증 객체(Authentication) 추출
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long memberId = Long.parseLong(claims.getSubject());
        String roleName = claims.get("role", String.class).replace("ROLE_", "");

        // Member 엔티티 스텁 생성
        Member member = Member.builder()
                .id(memberId)
                .role(MemberRole.valueOf(roleName))
                .build();

        MemberDetails principal = new MemberDetails(member);

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) { // 토큰 만료
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) { // 그 외 유효하지 않은 토큰
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
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