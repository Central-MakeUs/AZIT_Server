package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.auth.jwt.JwtProvider;
import com.youthexpedition.azit.modules.auth.application.port.in.SocialLoginUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.RefreshTokenPort;
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
    private final RefreshTokenPort refreshTokenPort;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResult login(SocialLoginCommand command) {
        SocialProfile profile = socialAuthPort.getSocialProfile(command.authorizationCode());

        // 기존 회원 확인 및 신규 회원 가입
        Member member = loadMemberPort.findBySocialInfo(profile.socialProvider(), profile.socialProviderId())
                .orElseGet(() -> registerNewMember(profile));

        // 토큰 생성
        AuthToken authToken = AuthToken.builder()
                .accessToken(jwtProvider.generateAccessToken(member.getId(), member.getRole()))
                .refreshToken(jwtProvider.generateRefreshToken(member.getId()))
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationSeconds())
                .build();

        // Redis에 Refresh Token 저장
        saveRefreshToken(member.getId(), authToken.refreshToken());

        return new AuthResult(authToken, member.getStatus());
    }

    private Member registerNewMember(SocialProfile profile) {
        Member newMember = Member.create(
                profile.socialProvider(),
                profile.socialProviderId(),
                profile.nickname(),
                profile.email(),
                profile.profileImageUrl()
        );
        return saveMemberPort.save(newMember);
    }

    private void saveRefreshToken(Long memberId, String refreshToken) {
        refreshTokenPort.save(memberId, refreshToken, jwtProvider.getRefreshTokenExpirationSeconds());
    }
}
