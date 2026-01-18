package com.youthexpedition.azit.modules.auth.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import org.springframework.security.core.Authentication;

public interface TokenProviderPort {
    String generateAccessToken(Long memberId, MemberRole role, MemberStatus status);
    String generateRefreshToken(Long memberId);
    Authentication getAuthentication(String accessToken);
    boolean validateToken(String token);
    Long extractMemberId(String token);
    long getRemainingExpirationMilliseconds(String token);
    long getAccessTokenExpirationSeconds();
    long getRefreshTokenExpirationSeconds();
}
