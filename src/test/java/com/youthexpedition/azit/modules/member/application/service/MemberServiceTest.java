package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.common.util.image.ImageUpdateUtil;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateMemberProfileCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyCrewResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.MemberResponseMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private LoadCrewPort loadCrewPort;
    @Mock
    private SocialAuthPort socialAuthPort;
    @Mock
    private TokenPort tokenPort;
    @Mock
    private MemberResponseMapper memberResponseMapper;
    @Mock
    private ImageUpdateUtil imageUpdateUtil;

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
            // validateWithdrawal + processCrewWithdrawal 에서 각 1회씩 호출
            verify(loadCrewMemberPort, times(2)).findAllByMemberId(memberId);
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
            // validateWithdrawal + processCrewWithdrawal 에서 각 1회씩 호출 후 tokenPort에서 예외 발생
            verify(loadCrewMemberPort, times(2)).findAllByMemberId(memberId);
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
    @DisplayName("프로필 수정 (닉네임 + 이미지 통합)")
    class UpdateMemberProfile {

        private final Long memberId = 1L;
        private final String currentImageUrl = "/profile/1/old_image.jpg";

        @Test
        @DisplayName("성공 - 닉네임만 수정 (이미지 URL 동일)")
        void updateMemberProfile_success_nicknameOnly() {
            // given
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", currentImageUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals(currentImageUrl, member.getProfileImageUrl());
            verify(imageUpdateUtil).update(eq(currentImageUrl), eq(currentImageUrl), eq(memberId), eq(true), any());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 새 커스텀 이미지로 변경 (콜백으로 URL 갱신)")
        void updateMemberProfile_success_newCustomImage() {
            // given
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            String finalS3Key = "profile/1/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doAnswer(inv -> {
                Consumer<String> updateUrl = inv.getArgument(4);
                updateUrl.accept("/" + finalS3Key);
                return null;
            }).when(imageUpdateUtil).update(eq(newTempUrl), eq(currentImageUrl), eq(memberId), eq(true), any());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals("/" + finalS3Key, member.getProfileImageUrl());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 기본 이미지로 변경 (콜백으로 URL 갱신)")
        void updateMemberProfile_success_defaultImage() {
            // given
            String defaultUrl = "/default/profile/2.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", defaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doAnswer(inv -> {
                Consumer<String> updateUrl = inv.getArgument(4);
                updateUrl.accept(defaultUrl);
                return null;
            }).when(imageUpdateUtil).update(eq(defaultUrl), eq(currentImageUrl), eq(memberId), eq(true), any());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals(defaultUrl, member.getProfileImageUrl());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 기본 이미지에서 다른 기본 이미지로 변경")
        void updateMemberProfile_success_defaultToOtherDefault() {
            // given
            String existingDefaultUrl = "/default/profile/1.png";
            String newDefaultUrl = "/default/profile/3.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, existingDefaultUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newDefaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doAnswer(inv -> {
                Consumer<String> updateUrl = inv.getArgument(4);
                updateUrl.accept(newDefaultUrl);
                return null;
            }).when(imageUpdateUtil).update(eq(newDefaultUrl), eq(existingDefaultUrl), eq(memberId), eq(true), any());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals(newDefaultUrl, member.getProfileImageUrl());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("실패 - temp 이미지가 S3에 존재하지 않음")
        void updateMemberProfile_fail_imageNotUploaded() {
            // given
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doThrow(new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED))
                    .when(imageUpdateUtil).update(any(), any(), any(), anyBoolean(), any());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.updateMemberProfile(memberId, command)
            );

            assertEquals(ImageErrorCode.IMAGE_NOT_UPLOADED.getCode(), exception.getErrorCode().getCode());
            verify(saveMemberPort, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 다른 사람이 업로드한 이미지 URL (소유권 불일치)")
        void updateMemberProfile_fail_imageOwnershipMismatch() {
            // given - memberId=1 이지만 이미지 경로의 entityId=99
            String otherMemberTempUrl = "https://images.azitcrew.com/temp/profile/99/2026-04-22_uuid.jpg";
            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, currentImageUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("newNickname", otherMemberTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doThrow(new BusinessException(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH))
                    .when(imageUpdateUtil).update(any(), any(), any(), anyBoolean(), any());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    memberService.updateMemberProfile(memberId, command)
            );

            assertEquals(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH.getCode(), exception.getErrorCode().getCode());
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
            doThrow(new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED))
                    .when(imageUpdateUtil).update(any(), any(), any(), anyBoolean(), any());

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
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("newNickname", member.getNickname());
            assertEquals(externalUrl, member.getProfileImageUrl());
            verify(imageUpdateUtil).update(eq(externalUrl), eq(externalUrl), eq(memberId), eq(true), any());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 소셜 로그인 외부 URL 사용자가 새 커스텀 이미지로 변경 (S3 삭제 없이 이동만)")
        void updateMemberProfile_success_externalUrlToCustomImage() {
            // given - 소셜 프로필(외부 URL)에서 새로 업로드한 커스텀 이미지로 교체
            String externalUrl = "https://k.kakao.com/profile/abc123.jpg";
            String newTempUrl = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";
            String finalS3Key = "profile/1/2026-04-22_uuid.jpg";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, externalUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newTempUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doAnswer(inv -> {
                Consumer<String> updateUrl = inv.getArgument(4);
                updateUrl.accept("/" + finalS3Key);
                return null;
            }).when(imageUpdateUtil).update(eq(newTempUrl), eq(externalUrl), eq(memberId), eq(true), any());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals("/" + finalS3Key, member.getProfileImageUrl());
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공 - 소셜 로그인 외부 URL 사용자가 기본 이미지로 변경")
        void updateMemberProfile_success_externalUrlToDefault() {
            // given
            String externalUrl = "https://k.kakao.com/profile/abc123.jpg";
            String newDefaultUrl = "/default/profile/2.png";

            Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, externalUrl);
            UpdateMemberProfileCommand command = UpdateMemberProfileCommand.of("nickname", newDefaultUrl);

            doReturn(Optional.of(member)).when(loadMemberPort).findById(memberId);
            doAnswer(inv -> {
                Consumer<String> updateUrl = inv.getArgument(4);
                updateUrl.accept(newDefaultUrl);
                return null;
            }).when(imageUpdateUtil).update(eq(newDefaultUrl), eq(externalUrl), eq(memberId), eq(true), any());
            doReturn(member).when(saveMemberPort).save(any(Member.class));

            // when
            memberService.updateMemberProfile(memberId, command);

            // then
            assertEquals(newDefaultUrl, member.getProfileImageUrl());
            verify(saveMemberPort, times(1)).save(member);
        }
    }

    @Nested
    @DisplayName("내 크루 목록 조회")
    class GetMyCrews {

        private final Long memberId = 1L;

        private CrewMember joinedCrewMember(Long crewId, CrewMemberRole role) {
            return CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .role(role)
                    .status(CrewMemberStatus.JOINED)
                    .build();
        }

        private CrewMember requestedCrewMember(Long crewId) {
            return CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.REQUESTED)
                    .build();
        }

        private Crew crew(Long crewId, String name) {
            return Crew.builder()
                    .id(crewId)
                    .name(name)
                    .invitationCode("CODE01")
                    .build();
        }

        @Test
        @DisplayName("성공 - 가입된 크루가 없으면 빈 목록 반환")
        void getMyCrews_success_returnsEmpty_whenNoActiveCrews() {
            // given
            given(loadCrewMemberPort.findAllByMemberId(memberId)).willReturn(List.of());

            // when
            List<MyCrewResponse> result = memberService.getMyCrews(memberId);

            // then
            assertThat(result).isEmpty();
            verify(loadCrewPort, never()).findAllByIds(any());
        }

        @Test
        @DisplayName("성공 - JOINED 크루만 있는 경우 목록 반환")
        void getMyCrews_success_returnsList_whenOnlyJoinedCrews() {
            // given
            CrewMember crewMember1 = joinedCrewMember(1L, CrewMemberRole.LEADER);
            CrewMember crewMember2 = joinedCrewMember(2L, CrewMemberRole.MEMBER);
            Crew crew1 = crew(1L, "크루A");
            Crew crew2 = crew(2L, "크루B");

            given(loadCrewMemberPort.findAllByMemberId(memberId)).willReturn(List.of(crewMember1, crewMember2));
            given(loadCrewPort.findAllByIds(List.of(1L, 2L))).willReturn(List.of(crew1, crew2));
            given(memberResponseMapper.toMyCrewResponse(crewMember1, crew1))
                    .willReturn(MyCrewResponse.of(1L, "크루A", null, CrewMemberRole.LEADER, CrewMemberStatus.JOINED, "CODE01"));
            given(memberResponseMapper.toMyCrewResponse(crewMember2, crew2))
                    .willReturn(MyCrewResponse.of(2L, "크루B", null, CrewMemberRole.MEMBER, CrewMemberStatus.JOINED, null));

            // when
            List<MyCrewResponse> result = memberService.getMyCrews(memberId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).memberStatus()).isEqualTo(CrewMemberStatus.JOINED);
            assertThat(result.get(0).memberRole()).isEqualTo(CrewMemberRole.LEADER);
            assertThat(result.get(1).memberStatus()).isEqualTo(CrewMemberStatus.JOINED);
            assertThat(result.get(1).memberRole()).isEqualTo(CrewMemberRole.MEMBER);
        }

        @Test
        @DisplayName("성공 - JOINED + REQUESTED 혼합 반환, EXITED 제외")
        void getMyCrews_success_returnsMixed_andExcludesExited() {
            // given
            CrewMember joinedMember = joinedCrewMember(1L, CrewMemberRole.MEMBER);
            CrewMember requestedMember = requestedCrewMember(2L);
            CrewMember exitedMember = CrewMember.builder()
                    .crewId(3L).memberId(memberId).status(CrewMemberStatus.EXITED).build();

            Crew crew1 = crew(1L, "크루A");
            Crew crew2 = crew(2L, "크루B");

            given(loadCrewMemberPort.findAllByMemberId(memberId))
                    .willReturn(List.of(joinedMember, requestedMember, exitedMember));
            given(loadCrewPort.findAllByIds(List.of(1L, 2L))).willReturn(List.of(crew1, crew2));
            given(memberResponseMapper.toMyCrewResponse(joinedMember, crew1))
                    .willReturn(MyCrewResponse.of(1L, "크루A", null, CrewMemberRole.MEMBER, CrewMemberStatus.JOINED, null));
            given(memberResponseMapper.toMyCrewResponse(requestedMember, crew2))
                    .willReturn(MyCrewResponse.of(2L, "크루B", null, null, CrewMemberStatus.REQUESTED, null));

            // when
            List<MyCrewResponse> result = memberService.getMyCrews(memberId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).noneMatch(r -> r.memberStatus() == CrewMemberStatus.EXITED);
        }
    }
}
