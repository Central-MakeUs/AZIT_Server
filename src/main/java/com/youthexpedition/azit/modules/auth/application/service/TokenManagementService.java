package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.TokenUseCase;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadTermsVersionPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.TermsVersion;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenManagementService implements TokenUseCase {
    private final LoadMemberPort loadMemberPort;
    private final TokenPort tokenPort;
    private final TokenProviderPort tokenProviderPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadTermsVersionPort loadTermsVersionPort;

    private static final String BLACKLIST_REASON_LOGOUT = "logout";
    private static final long PREV_TOKEN_TTL_SECONDS = 5;

    @Override
    public AuthResult reissue(String refreshToken) {

        // JWT 서명 검증 및 memberId 추출
        tokenProviderPort.validateToken(refreshToken);
        Long memberId = tokenProviderPort.extractMemberId(refreshToken);
        log.info("[reissue] Token validated, memberId extracted: {}", memberId);

        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 신규 토큰 생성 (Lua 스크립트 전달용으로 미리 생성)
        String newAccessToken = tokenProviderPort.generateAccessToken(member.getId(), member.getRole(), member.getStatus());
        String newRefreshToken = tokenProviderPort.generateRefreshToken(member.getId());

        // GET-COMPARE-SET을 Lua 스크립트로 원자적 수행
        boolean rotated = tokenPort.compareAndRotate(
                memberId, refreshToken, newRefreshToken,
                PREV_TOKEN_TTL_SECONDS, tokenProviderPort.getRefreshTokenExpirationSeconds()
        );

        if (!rotated) {
            // Lua 비교 실패: race condition 패배 or 탈취 의심
            String currentRT = tokenPort.findByMemberId(memberId)
                    .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED));

            boolean isGracePeriod = tokenPort.findPrevToken(memberId)
                    .map(prev -> prev.equals(refreshToken))
                    .orElse(false);

            if (!isGracePeriod) {
                tokenPort.deleteByMemberId(memberId);
                log.warn("[TOKEN_MANAGEMENT] memberId: {} 의 비정상적인 토큰 접근(탈취 의심)이 감지되어 모든 세션을 강제 종료합니다.", memberId);
                throw new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED);
            }

            // Race condition: 이미 교체된 currentRT를 그대로 사용하고 AT만 새로 발급
            log.info("[TOKEN_MANAGEMENT] memberId: {} 에서 race condition 이 발생했습니다.", memberId);
            AuthToken token = AuthToken.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(currentRT)
                    .accessTokenExpiresIn(tokenProviderPort.getAccessTokenExpirationSeconds())
                    .build();

            return AuthResult.builder()
                    .authToken(token)
                    .status(member.getStatus())
                    .crewId(resolveCrewId(member))
                    .needsTermsUpdate(hasRequiredTermsUpdate(member))
                    .build();
        }

        // 정상 경로
        AuthToken token = AuthToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(tokenProviderPort.getAccessTokenExpirationSeconds())
                .build();

        return AuthResult.builder()
                    .authToken(token)
                    .status(member.getStatus())
                    .crewId(resolveCrewId(member))
                    .needsTermsUpdate(hasRequiredTermsUpdate(member))
                    .build();
    }

    private Long resolveCrewId(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) return null;
        return loadCrewMemberPort.findRecentJoinedCrewMember(member.getId())
                .map(CrewMember::getCrewId)
                .orElse(null);
    }

    private boolean hasRequiredTermsUpdate(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) return false;
        List<TermsVersion> latestVersions = loadTermsVersionPort.findAllLatest();
        Set<Long> consentedVersionIds = loadTermsVersionPort.findConsentedVersionIdsByMemberId(member.getId());
        return latestVersions.stream()
                .filter(TermsVersion::isRequired)
                .anyMatch(v -> !consentedVersionIds.contains(v.getId()));
    }

    @Override
    public void logout(Long memberId, String accessToken) {
        log.info("[TOKEN_MANAGEMENT] memberId: {} 가 로그아웃하여 리프레시 토큰을 삭제합니다.", memberId);

        tokenPort.deleteByMemberId(memberId);
        tokenPort.addToBlacklist(accessToken, BLACKLIST_REASON_LOGOUT); // 블랙리스트에 추가
    }
}
