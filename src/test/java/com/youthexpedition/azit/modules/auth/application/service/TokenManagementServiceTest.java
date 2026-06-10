package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
import com.youthexpedition.azit.modules.auth.domain.model.AuthResult;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadTermsVersionPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.TermsVersion;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberRole;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import com.youthexpedition.azit.modules.member.domain.model.enums.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenManagementService 단위 테스트")
class TokenManagementServiceTest {

    @Mock private TokenPort tokenPort;
    @Mock private TokenProviderPort tokenProviderPort;
    @Mock private LoadMemberPort loadMemberPort;
    @Mock private LoadCrewMemberPort loadCrewMemberPort;
    @Mock private LoadTermsVersionPort loadTermsVersionPort;

    @InjectMocks
    private TokenManagementService tokenManagementService;

    private final Member activeMember = Member.builder()
            .id(1L)
            .socialProvider(SocialProvider.KAKAO)
            .socialProviderId("socialId")
            .nickname("testUser")
            .status(MemberStatus.ACTIVE)
            .role(MemberRole.MEMBER)
            .totalPoints(0L)
            .totalAttendanceCount(0)
            .build();

    private TermsVersion termsVersion(Long id, TermsType type, boolean isRequired, String version) {
        return TermsVersion.builder()
                .id(id)
                .termsType(type)
                .version(version)
                .isRequired(isRequired)
                .effectiveAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
    }

    private void stubReissue(Member member) {
        doReturn(true).when(tokenProviderPort).validateToken(any());
        doReturn(member.getId()).when(tokenProviderPort).extractMemberId(any());
        doReturn(Optional.of(member)).when(loadMemberPort).findById(member.getId());
        doReturn("newAccessToken").when(tokenProviderPort).generateAccessToken(any(), any(), any());
        doReturn("newRefreshToken").when(tokenProviderPort).generateRefreshToken(any());
        doReturn(3600L).when(tokenProviderPort).getAccessTokenExpirationSeconds();
        doReturn(604800L).when(tokenProviderPort).getRefreshTokenExpirationSeconds();
        doReturn(true).when(tokenPort).compareAndRotate(any(), any(), any(), anyLong(), anyLong());
        doReturn(Optional.empty()).when(loadCrewMemberPort).findRecentJoinedCrewMember(any());
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공")
        void logout_success() {
            // given
            Long memberId = 1L;
            String accessToken = "accessToken";

            // when
            tokenManagementService.logout(memberId, accessToken);

            // then
            verify(tokenPort, times(1)).deleteByMemberId(memberId);
            verify(tokenPort, times(1)).addToBlacklist(accessToken, "logout");
        }
    }

    @Nested
    @DisplayName("토큰 재발급 - 약관 버전 체크")
    class ReissueTermsCheck {

        @Test
        @DisplayName("모든 필수 약관에 동의한 경우 needsTermsUpdate = false")
        void reissue_needsTermsUpdate_false_whenAllRequiredTermsConsented() {
            // given
            stubReissue(activeMember);
            List<TermsVersion> latestVersions = List.of(
                    termsVersion(1L, TermsType.SERVICE, true, "1.0"),
                    termsVersion(2L, TermsType.PRIVACY, true, "1.0")
            );
            doReturn(latestVersions).when(loadTermsVersionPort).findAllLatest();
            doReturn(Set.of(1L, 2L)).when(loadTermsVersionPort).findConsentedVersionIdsByMemberId(1L);

            // when
            AuthResult result = tokenManagementService.reissue("refreshToken");

            // then
            assertFalse(result.needsTermsUpdate());
        }

        @Test
        @DisplayName("토큰 재발급 중 새 버전 필수 약관 배포 시 needsTermsUpdate = true")
        void reissue_needsTermsUpdate_true_whenNewRequiredTermsVersionReleasedDuringSession() {
            // given
            stubReissue(activeMember);
            List<TermsVersion> latestVersions = List.of(
                    termsVersion(10L, TermsType.SERVICE, true, "2.0"), // 새 버전 배포
                    termsVersion(2L, TermsType.PRIVACY, true, "1.0")
            );
            doReturn(latestVersions).when(loadTermsVersionPort).findAllLatest();
            // 멤버는 v1.0(id=1)에만 동의, 새로 배포된 v2.0(id=10)에는 미동의
            doReturn(Set.of(1L, 2L)).when(loadTermsVersionPort).findConsentedVersionIdsByMemberId(1L);

            // when
            AuthResult result = tokenManagementService.reissue("refreshToken");

            // then
            assertTrue(result.needsTermsUpdate());
        }
    }
}
