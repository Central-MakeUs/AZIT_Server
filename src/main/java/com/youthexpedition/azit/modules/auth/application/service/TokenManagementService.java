package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.TokenUseCase;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
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
    private final TokenPort tokenPort;
    private final TokenProviderPort tokenProviderPort;

    private static final String BLACKLIST_REASON_LOGOUT = "logout";

    @Override
    public AuthResult reissue(String refreshToken) {
        // 검증 및 memberId 추출
        tokenProviderPort.validateToken(refreshToken);
        Long memberId = tokenProviderPort.extractMemberId(refreshToken);

        // Redis의 RT와 비교
        String savedRT = tokenPort.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED));

        if (!savedRT.equals(refreshToken)) {
            tokenPort.deleteByMemberId(memberId);
            throw new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED);
        }

        // 신규 토큰 발급 및 Redis 업데이트
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = tokenProviderPort.generateAccessToken(member.getId(), member.getRole(), member.getStatus());
        String newRefreshToken = tokenProviderPort.generateRefreshToken(member.getId());

        tokenPort.save(member.getId(), newRefreshToken, tokenProviderPort.getRefreshTokenExpirationSeconds());

        AuthToken token = AuthToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(tokenProviderPort.getAccessTokenExpirationSeconds())
                .build();

        return new AuthResult(token, member.getStatus());
    }

    @Override
    public void logout(Long memberId, String accessToken) {
        tokenPort.deleteByMemberId(memberId);
        tokenPort.addToBlacklist(accessToken, BLACKLIST_REASON_LOGOUT); // 블랙리스트에 추가
    }
}
