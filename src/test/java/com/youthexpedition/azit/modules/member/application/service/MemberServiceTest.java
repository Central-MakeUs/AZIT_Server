package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private LoadMemberPort loadMemberPort;
    @Mock
    private SaveMemberPort saveMemberPort;
    @Mock
    private SocialAuthPort socialAuthPort;
    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        private final Long memberId = 1L;
        private final String accessToken = "testAccessToken";
        private final Member member = Member.create(SocialProvider.KAKAO, "socialId", "test@example.com", "password", true, "nickname");

        @Test
        @DisplayName("성공")
        void withdraw_success() {
            // given
            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doNothing().when(socialAuthPort).revoke(any());
            doNothing().when(tokenPort).deleteByMemberId(memberId);
            doNothing().when(tokenPort).addToBlacklist(anyString(), anyString());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.withdraw(memberId, accessToken);

            // then
            verify(loadMemberPort, times(1)).findById(memberId);
            verify(socialAuthPort, times(1)).revoke(any());
            verify(tokenPort, times(1)).deleteByMemberId(memberId);
            verify(tokenPort, times(1)).addToBlacklist(accessToken, "withdrawn");
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("실패 - 회원을 찾을 수 없음")
        void withdraw_fail_memberNotFound() {
            // given
            doReturn(Optional.empty()).when(loadMemberPort).findById(memberId);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.withdraw(memberId, accessToken)
            );

            verify(loadMemberPort, times(1)).findById(memberId);
            verify(socialAuthPort, never()).revoke(any());
            verify(tokenPort, never()).deleteByMemberId(anyLong());
            verify(tokenPort, never()).addToBlacklist(anyString(), anyString());
            verify(saveMemberPort, never()).save(any(Member.class));
            assertEquals(MemberErrorCode.MEMBER_NOT_FOUND.getCode(), exception.getErrorCode().getCode());
        }

        @Test
        @DisplayName("실패 - 소셜 연동 해제 실패")
        void withdraw_fail_socialRevokeFails() {
            // given
            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doThrow(new RuntimeException("Social revoke failed")).when(socialAuthPort).revoke(any());

            // when & then
            assertThrows(RuntimeException.class, () ->
                    memberService.withdraw(memberId, accessToken)
            );

            verify(loadMemberPort, times(1)).findById(memberId);
            verify(socialAuthPort, times(1)).revoke(any());
            verify(tokenPort, never()).deleteByMemberId(anyLong());
            verify(tokenPort, never()).addToBlacklist(anyString(), anyString());
            verify(saveMemberPort, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 토큰 삭제 실패")
        void withdraw_fail_tokenDeletionFails() {
            // given
            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doNothing().when(socialAuthPort).revoke(any());
            doThrow(new RuntimeException("Token deletion failed")).when(tokenPort).deleteByMemberId(memberId);

            // when & then
            assertThrows(RuntimeException.class, () ->
                    memberService.withdraw(memberId, accessToken)
            );

            verify(loadMemberPort, times(1)).findById(memberId);
            verify(socialAuthPort, times(1)).revoke(any());
            verify(tokenPort, times(1)).deleteByMemberId(memberId);
            verify(tokenPort, never()).addToBlacklist(anyString(), anyString());
            verify(saveMemberPort, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("약관 동의")
    class AgreeToTerms {

        private final Long memberId = 1L;
        private final Member member = Member.create(SocialProvider.KAKAO, "socialId", "test@example.com", "password", true, "nickname");

        @Test
        @DisplayName("성공")
        void agreeToTerms_success() {
            // given
            AgreeToTermsCommand command = new AgreeToTermsCommand(true, true, true, true, true, true);
            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.agreeToTerms(memberId, command);

            // then
            verify(loadMemberPort, times(1)).findById(memberId);
            verify(saveMemberPort, times(1)).save(member);
            assertTrue(member.isMarketingTermsAgreed());
            assertTrue(member.isNotificationAgreed());
            assertNotNull(member.getEssentialTermsAgreedAt());
        }

        @Test
        @DisplayName("실패 - 필수 약관 미동의")
        void agreeToTerms_fail_requiredTermsNotAgreed() {
            // given
            // 서비스 이용약관(serviceTermsAgreed)을 false로 설정
            AgreeToTermsCommand command = new AgreeToTermsCommand(false, true, true, true, false, false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.agreeToTerms(memberId, command)
            );

            assertEquals(MemberErrorCode.REQUIRED_TERMS_NOT_AGREED, exception.getErrorCode());
            verify(loadMemberPort, never()).findById(anyLong());
            verify(saveMemberPort, never()).save(any(Member.class));
        }
    }
}
