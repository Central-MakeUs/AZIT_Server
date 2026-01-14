package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.auth.jwt.JwtProvider;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.TokenUseCase;
import com.youthexpedition.azit.modules.auth.application.port.out.RefreshTokenPort;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenManagementService implements TokenUseCase {
    private final LoadMemberPort loadMemberPort;
    private final RefreshTokenPort refreshTokenPort;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResult reissue(String refreshToken) {
        // 검증 및 memberId 추출
        jwtProvider.validateToken(refreshToken);
        Long memberId = jwtProvider.extractMemberId(refreshToken);

        // Redis의 RT와 비교
        String savedRT = refreshTokenPort.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED));

        if (!savedRT.equals(refreshToken)) {
            refreshTokenPort.deleteByMemberId(memberId);
            throw new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED);
        }

        // 신규 토큰 발급 및 Redis 업데이트
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        String newAT = jwtProvider.generateAccessToken(member.getId(), member.getRole(), member.getStatus());
        String newRT = jwtProvider.generateRefreshToken(member.getId());

        refreshTokenPort.save(member.getId(), newRT, jwtProvider.getRefreshTokenExpirationSeconds());

        AuthToken token = AuthToken.builder()
                .accessToken(newAT)
                .refreshToken(newRT)
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationSeconds())
                .build();

        return new AuthResult(token, member.getStatus());
    }

    @Override
    public void logout(Long memberId) {
        refreshTokenPort.deleteByMemberId(memberId);
    }
}
