package com.youthexpedition.azit.infrastructure.auth.jwt;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider implements TokenProviderPort {

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
    @Override
    public String generateAccessToken(Long memberId, MemberRole role, MemberStatus status) {
        Instant now = Instant.now();
        Instant validity = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .subject(memberId.toString())
                .claim("role", "ROLE_" + role.name())
                .claim("status", "STATUS_" + status.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    @Override
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
    @Override
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long memberId = Long.parseLong(claims.getSubject());

        String roleName = claims.get("role", String.class);
        String statusName = claims.get("status", String.class);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roleName));
        authorities.add(new SimpleGrantedAuthority(statusName));

        // SecurityContext에 저장할 Member 생성
        Member member = Member.builder()
                .id(memberId)
                .role(MemberRole.valueOf(roleName.replace("ROLE_", "")))
                .status(MemberStatus.valueOf(statusName.replace("STATUS_", "")))
                .build();

        MemberDetails principal = new MemberDetails(member);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검사
    @Override
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

    @Override
    public Long extractMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // 블랙리스트 추가를 위해 남은 액세스 토큰 시간을 계산하는 메서드
    @Override
    public long getRemainingExpirationMilliseconds(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            return 0;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}