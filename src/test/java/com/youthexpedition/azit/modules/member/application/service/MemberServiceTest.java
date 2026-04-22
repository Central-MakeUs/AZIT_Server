package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.image.application.port.out.ImageStoragePort;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateMemberProfileCommand;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private LoadCrewMemberPort loadCrewMemberPort;
    @Mock
    private SocialAuthPort socialAuthPort;
    @Mock
    private TokenPort tokenPort;
    @Mock
    private ImageStoragePort imageStoragePort;
    @Mock
    private ImageUrlFormatUtil imageUrlFormatUtil;

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
            doReturn(List.of()).when(loadCrewMemberPort).findAllByMemberId(memberId);
            doNothing().when(socialAuthPort).revoke(any());
            doNothing().when(tokenPort).deleteByMemberId(memberId);
            doNothing().when(tokenPort).addToBlacklist(anyString(), anyString());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.withdraw(memberId, accessToken);

            // then
            verify(loadMemberPort, times(1)).findById(memberId);
            verify(loadCrewMemberPort, times(1)).findAllByMemberId(memberId);
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
            doReturn(List.of()).when(loadCrewMemberPort).findAllByMemberId(memberId);
            doNothing().when(socialAuthPort).revoke(any());
            doThrow(new RuntimeException("Token deletion failed")).when(tokenPort).deleteByMemberId(memberId);

            // when & then
            assertThrows(RuntimeException.class, () ->
                    memberService.withdraw(memberId, accessToken)
            );

            verify(loadMemberPort, times(1)).findById(memberId);
            verify(socialAuthPort, times(1)).revoke(any());
            verify(loadCrewMemberPort, times(1)).findAllByMemberId(memberId);
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
            verify(loadMemberPort, times(1)).findById(memberId);
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
            verify(loadMemberPort, times(1)).findById(memberId);
            verify(saveMemberPort, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("프로필 수정 (닉네임 + 이미지 통합)")
    class UpdateMemberProfile {

        private final Long memberId = 1L;
        private final String currentImageUrl = "/profile/1/old_image.jpg";
        private final String currentS3Key = "profile/1/old_image.jpg";

        @Test
        @DisplayName("성공 - 닉네임만 수정 (이미지 URL 동일)")
        void updateMemberProfile_success_nicknameOnly() {
            // given
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", currentImageUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals(currentImageUrl, member.getProfileImageUrl()); // 이미지 변경 없음
            verify(imageStoragePort, never()).exists(anyString());
            verify(imageStoragePort, never()).move(anyString(), anyString());
            verify(imageStoragePort, never()).delete(anyString());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 새 커스텀 이미지로 변경 (기존 커스텀 이미지 삭제 후 이동)")
        void updateMemberProfile_success_newCustomImage() {
            // given
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            String newTempS3Key = "temp/profile/1/2026-04-22_uuid.jpg";
            String finalS3Key = "profile/1/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(newTempS3Key).when(imageUrlFormatUtil).extractS3Key(newTempUrl);
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);
            doReturn(true).when(imageStoragePort).exists(newTempS3Key);
            doNothing().when(imageStoragePort).delete(currentS3Key);
            doNothing().when(imageStoragePort).move(newTempS3Key, finalS3Key);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals("/" + finalS3Key, member.getProfileImageUrl());
            verify(imageStoragePort, times(1)).delete(currentS3Key);
            verify(imageStoragePort, times(1)).move(newTempS3Key, finalS3Key);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 기본 이미지로 변경 (기존 커스텀 이미지 삭제)")
        void updateMemberProfile_success_defaultImage() {
            // given
            String defaultUrl = "/default/profile/2.png";
            String defaultS3Key = "default/profile/2.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", defaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(defaultS3Key).when(imageUrlFormatUtil).extractS3Key(defaultUrl);
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);
            doNothing().when(imageStoragePort).delete(currentS3Key);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals("/" + defaultS3Key, member.getProfileImageUrl());
            verify(imageStoragePort, times(1)).delete(currentS3Key);
            verify(imageStoragePort, never()).move(anyString(), anyString());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 이미 기본 이미지 사용 중에 다른 기본 이미지로 변경 (S3 삭제 없음)")
        void updateMemberProfile_success_defaultToOtherDefault() {
            // given
            String existingDefaultUrl = "/default/profile/1.png";
            String existingDefaultS3Key = "default/profile/1.png";
            String newDefaultUrl = "/default/profile/3.png";
            String newDefaultS3Key = "default/profile/3.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, existingDefaultUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newDefaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(newDefaultS3Key).when(imageUrlFormatUtil).extractS3Key(newDefaultUrl);
            doReturn(existingDefaultS3Key).when(imageUrlFormatUtil).extractS3Key(existingDefaultUrl);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("/" + newDefaultS3Key, member.getProfileImageUrl());
            verify(imageStoragePort, never()).delete(anyString()); // 기본 이미지는 S3 삭제 안함
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("실패 - temp 이미지가 S3에 존재하지 않음")
        void updateMemberProfile_fail_imageNotUploaded() {
            // given
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            String newTempS3Key = "temp/profile/1/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(newTempS3Key).when(imageUrlFormatUtil).extractS3Key(newTempUrl);
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);
            doReturn(false).when(imageStoragePort).exists(newTempS3Key); // S3에 없음

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.updateMemberProfile(memberId, command)
            );

            assertEquals(ImageErrorCode.IMAGE_NOT_UPLOADED.getCode(), exception.getErrorCode().getCode());
            verify(imageStoragePort, never()).move(anyString(), anyString());
            verify(saveMemberPort, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 다른 사람이 업로드한 이미지 URL (소유권 불일치)")
        void updateMemberProfile_fail_imageOwnershipMismatch() {
            // given - memberId=1 이지만 이미지 경로의 entityId=99
            String otherMemberTempUrl = "https://images.azitcrew.com/temp/profile/99/2026-04-22_uuid.jpg";
            String otherMemberTempS3Key = "temp/profile/99/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", otherMemberTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(otherMemberTempS3Key).when(imageUrlFormatUtil).extractS3Key(otherMemberTempUrl);
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);
            doReturn(true).when(imageStoragePort).exists(otherMemberTempS3Key);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.updateMemberProfile(memberId, command)
            );

            assertEquals(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH.getCode(), exception.getErrorCode().getCode());
            verify(imageStoragePort, never()).move(anyString(), anyString());
            verify(saveMemberPort, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 이미지 URL (S3 키 추출 불가)")
        void updateMemberProfile_fail_invalidImageUrl() {
            // given
            String invalidUrl = "not-a-valid-url";
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", invalidUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(null).when(imageUrlFormatUtil).extractS3Key(invalidUrl); // 추출 불가
            doReturn(currentS3Key).when(imageUrlFormatUtil).extractS3Key(currentImageUrl);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.updateMemberProfile(memberId, command)
            );

            assertEquals(ImageErrorCode.IMAGE_NOT_UPLOADED.getCode(), exception.getErrorCode().getCode());
            verify(saveMemberPort, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("성공 - 소셜 로그인 외부 URL 사용자가 닉네임만 수정 (이미지 URL 동일)")
        void updateMemberProfile_success_externalUrlUnchanged() {
            // given - 카카오 프로필 이미지(외부 URL)를 그대로 유지
            String externalUrl = "https://k.kakao.com/profile/abc123.jpg";
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, externalUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", externalUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(null).when(imageUrlFormatUtil).extractS3Key(externalUrl); // 외부 URL → S3 키 null
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals(externalUrl, member.getProfileImageUrl()); // 이미지 그대로 유지
            verify(imageStoragePort, never()).exists(anyString());
            verify(imageStoragePort, never()).move(anyString(), anyString());
            verify(imageStoragePort, never()).delete(anyString());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 소셜 로그인 외부 URL 사용자가 새 커스텀 이미지로 변경 (S3 삭제 없이 이동만)")
        void updateMemberProfile_success_externalUrlToCustomImage() {
            // given - 소셜 프로필(외부 URL)에서 새로 업로드한 커스텀 이미지로 교체
            String externalUrl = "https://k.kakao.com/profile/abc123.jpg";
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            String newTempS3Key = "temp/profile/1/2026-04-22_uuid.jpg";
            String finalS3Key = "profile/1/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, externalUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(newTempS3Key).when(imageUrlFormatUtil).extractS3Key(newTempUrl);
            doReturn(null).when(imageUrlFormatUtil).extractS3Key(externalUrl); // 외부 URL → S3 키 null
            doReturn(true).when(imageStoragePort).exists(newTempS3Key);
            doNothing().when(imageStoragePort).move(newTempS3Key, finalS3Key);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("/" + finalS3Key, member.getProfileImageUrl());
            verify(imageStoragePort, never()).delete(anyString()); // 외부 URL은 S3 삭제 대상 아님
            verify(imageStoragePort, times(1)).move(newTempS3Key, finalS3Key);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 소셜 로그인 외부 URL 사용자가 기본 이미지로 변경")
        void updateMemberProfile_success_externalUrlToDefault() {
            // given
            String externalUrl = "https://k.kakao.com/profile/abc123.jpg";
            String newDefaultUrl = "/default/profile/2.png";
            String newDefaultS3Key = "default/profile/2.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, externalUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newDefaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(newDefaultS3Key).when(imageUrlFormatUtil).extractS3Key(newDefaultUrl);
            doReturn(null).when(imageUrlFormatUtil).extractS3Key(externalUrl); // 외부 URL → S3 키 null
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("/" + newDefaultS3Key, member.getProfileImageUrl());
            verify(imageStoragePort, never()).delete(anyString()); // 외부 URL은 S3 삭제 대상 아님
            verify(saveMemberPort, times(1)).save(member);
        }
    }
}
