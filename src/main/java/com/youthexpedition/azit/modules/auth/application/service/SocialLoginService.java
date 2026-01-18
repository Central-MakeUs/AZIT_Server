package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.auth.jwt.JwtProvider;
import com.youthexpedition.azit.modules.auth.application.port.in.SocialLoginUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginService implements SocialLoginUseCase {
    private final SocialAuthPort socialAuthPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenPort tokenPort;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResult login(SocialLoginCommand command) {
        SocialProfile profile = socialAuthPort.getSocialProfile(command);

        // 기존 회원 확인 및 신규 회원 가입
        Member member = upsertMember(profile);
        Member savedMember = saveMemberPort.save(member);

        // 토큰 생성
        AuthToken authToken = AuthToken.builder()
                .accessToken(jwtProvider.generateAccessToken(savedMember.getId(), savedMember.getRole(), savedMember.getStatus()))
                .refreshToken(jwtProvider.generateRefreshToken(savedMember.getId()))
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationSeconds())
                .build();

        // Redis에 Refresh Token 저장
        saveRefreshToken(savedMember.getId(), authToken.refreshToken());

        return new AuthResult(authToken, savedMember.getStatus());
    }

    private Member upsertMember(SocialProfile profile) {
        Member member = loadMemberPort.findBySocialInfo(profile.socialProvider(), profile.socialProviderId())
                .orElseGet(() -> Member.create(
                        profile.socialProvider(),
                        profile.socialProviderId(),
                        profile.nickname(),
                        profile.email(),
                        profile.profileImageUrl()
                ));

        // 애플 리프레시 토큰이 존재하는 경우 최신값으로 업데이트
        if (profile.refreshToken() != null) {
            member.updateAppleRefreshToken(profile.refreshToken());
        }

        return member;
    }

    private void saveRefreshToken(Long memberId, String refreshToken) {
        tokenPort.save(memberId, refreshToken, jwtProvider.getRefreshTokenExpirationSeconds());
    }
}
