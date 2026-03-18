package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.infrastructure.auth.jwt.JwtProvider;
import com.youthexpedition.azit.modules.auth.application.port.in.SocialLoginUseCase;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.auth.domain.model.AuthToken;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.provider.ProfileImageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SocialLoginService implements SocialLoginUseCase {
    private final SocialAuthPort socialAuthPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenPort tokenPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final JwtProvider jwtProvider;
    private final ProfileImageProvider profileImageProvider;

    @Override
    public AuthResult login(SocialLoginCommand command) {
        SocialProfile profile = socialAuthPort.getSocialProfile(command);

        // 기존 회원 확인 및 신규 회원 가입
        Member member = upsertMember(profile);
        Member savedMember = saveMemberPort.save(member);

        Long crewId = null;
        // 크루 ID 필요한지 체크 후 가장 최근에 가입한 크루 조회
        if (member.getStatus().isCrewInfoRequired()) {
            crewId = loadCrewMemberPort.findRecentJoinedCrewMember(savedMember.getId())
                    .map(CrewMember::getCrewId)
                    .orElse(null);
        }

        // 토큰 생성
        AuthToken authToken = AuthToken.builder()
                .accessToken(jwtProvider.generateAccessToken(savedMember.getId(), savedMember.getRole(), savedMember.getStatus()))
                .refreshToken(jwtProvider.generateRefreshToken(savedMember.getId()))
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationSeconds())
                .build();

        // Redis에 Refresh Token 저장
        saveRefreshToken(savedMember.getId(), authToken.refreshToken());

        return new AuthResult(authToken, savedMember.getStatus(), crewId);
    }

    private Member upsertMember(SocialProfile profile) {
        Member member = loadMemberPort.findBySocialInfo(profile.socialProvider(), profile.socialProviderId())
                .orElseGet(() -> {
                    // 제공받은 프로필 이미지가 없을 경우 랜덤으로 기본 이미지 설정
                    String profileImageUrl = profile.profileImageUrl();
                    if (profileImageUrl == null || profileImageUrl.isBlank()) {
                        profileImageUrl = profileImageProvider.getRandomDefaultImage();
                    }

                    return Member.create(
                            profile.socialProvider(),
                            profile.socialProviderId(),
                            profile.nickname(),
                            profile.email(),
                            profile.isEmailSharingEnabled(),
                            profileImageUrl
                    );
                });

        // 탈퇴한 회원인 경우 재활성화
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            log.info("[SOCIAL_LOGIN] 탈퇴했던 회원 memberId: {}, socialProviderId: {} 가 재로그인하여 계정이 재활성화 되었습니다.", member.getId(), profile.socialProviderId());
            member.reactivate();
        }

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
