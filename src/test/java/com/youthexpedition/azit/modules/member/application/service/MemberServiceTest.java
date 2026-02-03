package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
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
    private SaveCrewMemberPort saveCrewMemberPort;
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

    @Nested
    @DisplayName("회원 상태 확정 (결과 확인)")
    class ConfirmMemberStatus {

        private final Long memberId = 1L;

        @Test
        @DisplayName("성공 - 승인 대기 상태에서 정회원(ACTIVE)으로 전환")
        void confirmMemberStatus_success_toActive() {
            // given
            // 1. 승인 확인 대기 상태의 멤버 생성 (Member.java에 정의된 로직 기반)
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");

            // 가입 신청 단계 -> 리더 승인 단계 순서대로 호출
            member.completeTermsAgreement(true, true); // PENDING_ONBOARDING
            member.applyForJoin(); // WAITING_FOR_APPROVE
            member.approveJoin(); // APPROVED_PENDING_CONFIRM

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.confirmMemberStatus(memberId);

            // then
            assertEquals(MemberStatus.ACTIVE, member.getStatus()); // 정회원으로 변경되었는지 확인
            verify(loadMemberPort, times(1)).findById(memberId);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 거절 확인 후 다시 온보딩 대기(PENDING_ONBOARDING)로 전환")
        void confirmMemberStatus_success_toPendingOnboarding() {
            // given
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");
            member.completeTermsAgreement(true, true);
            member.applyForJoin();
            member.rejectJoin(); // REJECTED_PENDING_CONFIRM 상태로 생성

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.confirmMemberStatus(memberId);

            // then
            assertEquals(MemberStatus.PENDING_ONBOARDING, member.getStatus()); // 다시 처음으로 돌아갔는지 확인
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("실패 - 확정 가능한 상태가 아닐 때 예외 발생")
        void confirmMemberStatus_fail_invalidStatus() {
            // given
            // 확정할 수 없는 상태(예: 처음 가입한 상태)의 멤버
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.confirmMemberStatus(memberId)
            );

            assertEquals(MemberErrorCode.INVALID_MEMBER_STATUS, exception.getErrorCode());
            verify(saveMemberPort, never()).save(any(Member.class));
        }
    }
}
