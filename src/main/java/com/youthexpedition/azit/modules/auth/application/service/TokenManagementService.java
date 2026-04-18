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
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class TokenManagementService implements TokenUseCase {
    private final LoadMemberPort loadMemberPort;
    private final TokenPort tokenPort;
    private final TokenProviderPort tokenProviderPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final Counter raceConditionCounter;

    private static final String BLACKLIST_REASON_LOGOUT = "logout";
    private static final long PREV_TOKEN_TTL_SECONDS = 10;

    public TokenManagementService(LoadMemberPort loadMemberPort, TokenPort tokenPort,
                                  TokenProviderPort tokenProviderPort, LoadCrewMemberPort loadCrewMemberPort,
                                  MeterRegistry meterRegistry) {
        this.loadMemberPort = loadMemberPort;
        this.tokenPort = tokenPort;
        this.tokenProviderPort = tokenProviderPort;
        this.loadCrewMemberPort = loadCrewMemberPort;
        this.raceConditionCounter = Counter.builder("auth.token.race_condition")
                .description("RTR 재발급 중 race condition 발생 횟수")
                .register(meterRegistry);
    }

    @Override
    public AuthResult reissue(String refreshToken) {

        // 검증 및 memberId 추출
        tokenProviderPort.validateToken(refreshToken);
        Long memberId = tokenProviderPort.extractMemberId(refreshToken);
        log.info("[reissue] Token validated, memberId extracted: {}", memberId);

        // Redis의 RT와 비교
        String savedRT = tokenPort.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED));

        if (!savedRT.equals(refreshToken)) {
            // 10초 내 재요청(race condition)인지 확인
            boolean isGracePeriod = tokenPort.findPrevToken(memberId)
                    .map(prev -> prev.equals(refreshToken))
                    .orElse(false);

            if (!isGracePeriod) {
                tokenPort.deleteByMemberId(memberId);
                log.warn("[TOKEN_MANAGEMENT] memberId: {} 의 비정상적인 토큰 접근(탈취 의심)이 감지되어 모든 세션을 강제 종료합니다.", memberId);
                throw new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED);
            }

            // Race condition: 이미 교체된 savedRT(RT2)를 그대로 사용하고 AT만 새로 발급
            log.info("[TOKEN_MANAGEMENT] memberId: {} 에서 race condition 이 발생했습니다.", memberId);
            raceConditionCounter.increment();
            Member member = loadMemberPort.findById(memberId)
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            String newAccessToken = tokenProviderPort.generateAccessToken(member.getId(), member.getRole(), member.getStatus());
            AuthToken token = AuthToken.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(savedRT)
                    .accessTokenExpiresIn(tokenProviderPort.getAccessTokenExpirationSeconds())
                    .build();

            Long crewId = resolveCrewId(member);
            return new AuthResult(token, member.getStatus(), crewId);
        }

        // 정상 경로: 신규 토큰 발급 및 Redis 업데이트
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = tokenProviderPort.generateAccessToken(member.getId(), member.getRole(), member.getStatus());
        String newRefreshToken = tokenProviderPort.generateRefreshToken(member.getId());

        tokenPort.savePrevToken(memberId, refreshToken, PREV_TOKEN_TTL_SECONDS); // 직전 RT 보관 (10초)
        tokenPort.save(member.getId(), newRefreshToken, tokenProviderPort.getRefreshTokenExpirationSeconds());

        AuthToken token = AuthToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(tokenProviderPort.getAccessTokenExpirationSeconds())
                .build();

        Long crewId = resolveCrewId(member);
        return new AuthResult(token, member.getStatus(), crewId);
    }

    private Long resolveCrewId(Member member) {
        if (!member.getStatus().isCrewInfoRequired()) return null;
        return loadCrewMemberPort.findRecentJoinedCrewMember(member.getId())
                .map(CrewMember::getCrewId)
                .orElse(null);
    }

    @Override
    public void logout(Long memberId, String accessToken) {
        log.info("[TOKEN_MANAGEMENT] memberId: {} 가 로그아웃하여 리프레시 토큰을 삭제합니다.", memberId);

        tokenPort.deleteByMemberId(memberId);
        tokenPort.addToBlacklist(accessToken, BLACKLIST_REASON_LOGOUT); // 블랙리스트에 추가
    }
}
