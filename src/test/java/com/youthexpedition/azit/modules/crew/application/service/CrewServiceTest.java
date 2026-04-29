package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.InvitationCodeResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewMemberResponseMapper;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewResponseMapper;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.crew.domain.model.provider.CrewImageProvider;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @Mock
    private SaveCrewPort saveCrewPort;
    @Mock
    private LoadCrewPort loadCrewPort;
    @Mock
    private SaveCrewMemberPort saveCrewMemberPort;
    @Mock
    private LoadCrewMemberPort loadCrewMemberPort;
    @Mock
    private SaveMemberPort saveMemberPort;
    @Mock
    private LoadMemberPort loadMemberPort;
    @Mock
    private CrewMemberResponseMapper crewMemberResponseMapper;
    @Mock
    private CrewResponseMapper crewResponseMapper;
    @Mock
    private CrewImageProvider crewImageProvider;
    @InjectMocks
    private CrewService crewService;

    @Test
    @DisplayName("크루 생성 완료 시 초대 코드를 포함한 DTO를 반환하고, 멤버 상태가 ACTIVE로 변경된다.")
    void createCrew_Service_Success() {
        // given
        Long leaderId = 1L;
        CreateCrewCommand command = CreateCrewCommand.of("아지트 러닝크루", "RUNNING", "SEOUL", leaderId);
        given(crewImageProvider.getCrewDefaultImage()).willReturn("defaultImageUrl");

        Crew mockCrew = Crew.builder()
                .id(100L)
                .imageUrl("imageUrl")
                .invitationCode("ABC123")
                .memberCount(1)
                .build();
        given(saveCrewPort.save(any(Crew.class))).willReturn(mockCrew);

        Member member = Member.builder()
                .id(leaderId)
                .status(MemberStatus.PENDING_ONBOARDING)
                .build();
        given(loadMemberPort.findById(leaderId)).willReturn(Optional.of(member));

        given(crewResponseMapper.toCreateResponse(any())).willReturn(new CreateCrewResponse("ABC123", "imageUrl"));

        // when
        CreateCrewResponse response = crewService.createCrew(command);

        // then
        assertThat(response.invitationCode()).isEqualTo("ABC123");
        verify(saveCrewPort, times(1)).save(any(Crew.class));
        verify(saveCrewMemberPort, times(1)).save(argThat(crewMember ->
                crewMember.getRole() == CrewMemberRole.LEADER &&
                        crewMember.getStatus() == CrewMemberStatus.JOINED
        ));

        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE); // 상태가 ACTIVE로 변했는지 확인
        verify(saveMemberPort, times(1)).save(member); // 변경된 멤버 정보가 저장되었는지 확인
    }

    @Nested
    @DisplayName("가입 요청 테스트")
    class JoinCrewTest {

        @Test
        @DisplayName("신규 가입 요청 시 REQUESTED 상태로 등록된다.")
        void joinCrew_NewMember_Success() {
            // given
            Long crewId = 100L;
            Long memberId = 1L;
            String invitationCode = "ABC123";
            JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

            Crew mockCrew = Crew.builder().id(crewId).invitationCode(invitationCode).build();
            given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

            // 기존 가입 내역 없음
            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.PENDING_ONBOARDING)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.empty());

            // when
            crewService.joinCrew(command);

            // then
            verify(saveCrewMemberPort, times(1)).save(argThat(crewMember ->
                    crewMember.getStatus() == CrewMemberStatus.REQUESTED
            ));
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WAITING_FOR_APPROVE);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("탈퇴(EXITED) 상태였던 회원이 재가입하면 REQUESTED 상태로 업데이트된다.")
        void joinCrew_ReJoin_Success() {
            // given
            Long crewId = 100L;
            Long memberId = 1L;
            String invitationCode = "ABC123";
            JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

            Crew mockCrew = Crew.builder().id(crewId).invitationCode(invitationCode).build();
            given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

            // 기존에 탈퇴(EXITED) 상태인 멤버
            CrewMember exitedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.EXITED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(exitedMember));

            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.PENDING_ONBOARDING)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));

            // when
            crewService.joinCrew(command);

            // then
            assertThat(exitedMember.getStatus()).isEqualTo(CrewMemberStatus.REQUESTED);
            verify(saveCrewMemberPort, times(1)).save(exitedMember);
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WAITING_FOR_APPROVE);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("이미 대기(REQUESTED) 중이거나 가입(JOINED)된 상태면 예외가 발생한다.")
        void joinCrew_Fail_AlreadyJoined() {
            // given
            Long memberId = 1L;
            Long crewId = 100L;
            String invitationCode = "ABC123";
            JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

            Crew mockCrew = Crew.builder().id(crewId).build();
            given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

            // 이미 가입 완료된 상태
            CrewMember joinedMember = CrewMember.builder().status(CrewMemberStatus.JOINED).build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(joinedMember));

            // when & then
            assertThatThrownBy(() -> crewService.joinCrew(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.ALREADY_JOINED_CREW.getMessage());
        }

        @Test
        @DisplayName("크루 가입 요청 시 존재하지 않는 크루 코드면 예외가 발생한다.")
        void joinCrew_Fail_CrewNotFound() {
            // given
            String wrongCode = "WRONG_CODE";
            JoinCrewCommand command = JoinCrewCommand.of(1L, wrongCode);

            given(loadCrewPort.findByInvitationCode(wrongCode)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.joinCrew(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.CREW_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 취소(CANCELLED) 후 24시간 이내에 재신청하면 CANCEL_REJOINING_COOLDOWN 예외가 발생한다.")
        void joinCrew_Fail_CancelCooldown() {
            // given
            Long crewId = 100L;
            Long memberId = 1L;
            String invitationCode = "ABC123";
            JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

            Crew mockCrew = Crew.builder().id(crewId).invitationCode(invitationCode).build();
            given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

            CrewMember cancelledMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.CANCELLED)
                    .cancelledAt(LocalDateTime.now().minusHours(1)) // 1시간 전 취소 (쿨다운 중)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(cancelledMember));

            // when & then
            assertThatThrownBy(() -> crewService.joinCrew(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.CANCEL_REJOINING_COOLDOWN.getMessage());
        }

        @Test
        @DisplayName("성공: 취소(CANCELLED) 후 24시간이 지나면 재신청이 가능하고 REQUESTED 상태로 변경된다.")
        void joinCrew_Success_AfterCancelCooldown() {
            // given
            Long crewId = 100L;
            Long memberId = 1L;
            String invitationCode = "ABC123";
            JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

            Crew mockCrew = Crew.builder().id(crewId).invitationCode(invitationCode).build();
            given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

            CrewMember cancelledMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.CANCELLED)
                    .cancelledAt(LocalDateTime.now().minusHours(25)) // 25시간 전 취소 (쿨다운 종료)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(cancelledMember));

            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.PENDING_ONBOARDING)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));

            // when
            crewService.joinCrew(command);

            // then
            assertThat(cancelledMember.getStatus()).isEqualTo(CrewMemberStatus.REQUESTED);
            verify(saveCrewMemberPort, times(1)).save(cancelledMember);
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WAITING_FOR_APPROVE);
        }
    }

    @Nested
    @DisplayName("가입 신청 취소 테스트")
    class CancelJoinRequestTest {

        private final Long crewId = 100L;
        private final Long memberId = 1L;

        @Test
        @DisplayName("성공: REQUESTED 상태일 때 취소하면 CANCELLED 상태로 변경되고 cancelledAt이 기록된다.")
        void cancelJoinRequest_Success() {
            // given
            CrewMember requestedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .role(CrewMemberRole.MEMBER)
                    .status(CrewMemberStatus.REQUESTED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(requestedMember));
            given(loadCrewMemberPort.countJoinedCrewsByMemberId(memberId)).willReturn(0L);

            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.WAITING_FOR_APPROVE)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));

            // when
            crewService.cancelJoinRequest(crewId, memberId);

            // then
            assertThat(requestedMember.getStatus()).isEqualTo(CrewMemberStatus.CANCELLED);
            assertThat(requestedMember.getCancelledAt()).isNotNull();
            verify(saveCrewMemberPort, times(1)).save(requestedMember);
        }

        @Test
        @DisplayName("성공: 다른 가입 크루가 없으면 멤버 상태가 PENDING_ONBOARDING으로 변경된다.")
        void cancelJoinRequest_Success_NoOtherCrews_ResetToOnboarding() {
            // given
            CrewMember requestedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .role(CrewMemberRole.MEMBER)
                    .status(CrewMemberStatus.REQUESTED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(requestedMember));
            given(loadCrewMemberPort.countJoinedCrewsByMemberId(memberId)).willReturn(0L);

            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.WAITING_FOR_APPROVE)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));

            // when
            crewService.cancelJoinRequest(crewId, memberId);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING_ONBOARDING);
            verify(saveMemberPort, times(1)).save(member);
        }

        @Test
        @DisplayName("성공: 다른 가입 크루가 있으면 멤버 상태가 ACTIVE로 유지된다.")
        void cancelJoinRequest_Success_HasOtherCrews_KeepsActive() {
            // given
            CrewMember requestedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .role(CrewMemberRole.MEMBER)
                    .status(CrewMemberStatus.REQUESTED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(requestedMember));
            given(loadCrewMemberPort.countJoinedCrewsByMemberId(memberId)).willReturn(1L); // 다른 크루에 가입 중

            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.WAITING_FOR_APPROVE)
                    .build();
            given(loadMemberPort.findById(memberId)).willReturn(Optional.of(member));

            // when
            crewService.cancelJoinRequest(crewId, memberId);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WAITING_FOR_APPROVE); // 상태 변경 없음
            verify(saveMemberPort, never()).save(any());
        }

        @Test
        @DisplayName("실패: 가입 신청 내역이 없으면 JOIN_REQUEST_NOT_FOUND 예외가 발생한다.")
        void cancelJoinRequest_Fail_NoRequest() {
            // given
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.cancelJoinRequest(crewId, memberId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.JOIN_REQUEST_NOT_FOUND.getMessage());

            verify(saveCrewMemberPort, never()).save(any());
            verify(saveMemberPort, never()).save(any());
        }

        @Test
        @DisplayName("실패: 이미 승인된(JOINED) 상태에서 취소하면 JOIN_REQUEST_NOT_FOUND 예외가 발생한다.")
        void cancelJoinRequest_Fail_AlreadyJoined() {
            // given
            CrewMember joinedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(joinedMember));

            // when & then
            assertThatThrownBy(() -> crewService.cancelJoinRequest(crewId, memberId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.JOIN_REQUEST_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 이미 거절된(REJECTED) 상태에서 취소하면 JOIN_REQUEST_NOT_FOUND 예외가 발생한다.")
        void cancelJoinRequest_Fail_AlreadyRejected() {
            // given
            CrewMember rejectedMember = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .status(CrewMemberStatus.REJECTED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(rejectedMember));

            // when & then
            assertThatThrownBy(() -> crewService.cancelJoinRequest(crewId, memberId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.JOIN_REQUEST_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("가입 요청 승인 및 거절 테스트")
    class ProcessJoinRequestsTest {

        @Test
        @DisplayName("리더가 가입 요청을 승인하면, 신청자의 상태가 JOINED로 변경되고 신청자의 상태가 APPROVED_PENDING_CONFIRM이 된다.")
        void approveJoinRequest_Success() {
            // given
            Long crewId = 100L;
            Long leaderId = 1L;
            Long targetMemberId = 2L;
            ProcessJoinCommand command = ProcessJoinCommand.of(crewId, targetMemberId, leaderId);

            // 리더 권한 설정
            CrewMember leader = CrewMember.builder()
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));

            // 가입 대상자 상태 설정 (상태: REQUESTED)
            CrewMember targetCrewMember = CrewMember.builder()
                    .status(CrewMemberStatus.REQUESTED).build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, targetMemberId)).willReturn(Optional.of(targetCrewMember));

            Crew mockCrew = Crew.builder()
                    .id(crewId)
                    .memberCount(0)
                    .build();
            given(loadCrewPort.findById(crewId)).willReturn(Optional.of(mockCrew));

            Member targetMember = Member.builder()
                    .id(targetMemberId)
                    .status(MemberStatus.WAITING_FOR_APPROVE)
                    .build();
            given(loadMemberPort.findById(targetMemberId)).willReturn(Optional.of(targetMember));

            // when
            crewService.approveJoinRequest(command);

            // then
            assertThat(targetCrewMember.getStatus()).isEqualTo(CrewMemberStatus.JOINED); // 크루 상태 변경 확인
            assertThat(targetMember.getStatus()).isEqualTo(MemberStatus.APPROVED_PENDING_CONFIRM); // 정회원 전환 확인

            verify(saveCrewMemberPort, times(1)).save(targetCrewMember);
            verify(saveMemberPort, times(1)).save(targetMember);
        }

        @Test
        @DisplayName("리더가 아닌 사용자가 승인을 시도하면 FORBIDDEN 에러가 발생한다.")
        void approveJoinRequest_Fail_Forbidden() {
            // given
            Long crewId = 100L;
            Long notLeaderId = 999L;
            ProcessJoinCommand command = ProcessJoinCommand.of(crewId, 2L, notLeaderId);

            // 일반 멤버로 모킹
            CrewMember member = CrewMember.builder()
                    .role(CrewMemberRole.MEMBER).build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> crewService.approveJoinRequest(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("리더가 가입 요청을 거절하면, 신청자의 상태가 REJECTED로 변경된다.")
        void rejectJoinRequest_Success() {
            // given
            Long crewId = 100L;
            Long leaderId = 1L;
            Long targetMemberId = 2L;
            ProcessJoinCommand command = ProcessJoinCommand.of(crewId, targetMemberId, leaderId);

            // 리더 권한 설정
            CrewMember leader = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(leaderId)
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));

            // 가입 대상자 상태 설정 (상태: REQUESTED)
            CrewMember targetCrewMember = CrewMember.builder()
                    .status(CrewMemberStatus.REQUESTED).build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, targetMemberId)).willReturn(Optional.of(targetCrewMember));

            Member targetMember = Member.builder()
                    .id(targetMemberId)
                    .status(MemberStatus.WAITING_FOR_APPROVE)
                    .build();
            given(loadMemberPort.findById(targetMemberId)).willReturn(Optional.of(targetMember));

            // when
            crewService.rejectJoinRequest(command);

            // then
            // 크루 가입 신청 상태가 REJECTED로 변경되었는지 확인
            assertThat(targetCrewMember.getStatus()).isEqualTo(CrewMemberStatus.REJECTED);
            verify(saveCrewMemberPort, times(1)).save(targetCrewMember);

            // 회원 상태가 REJECTED_PENDING_CONFIRM으로 변경되고 저장되었는지 확인
            assertThat(targetMember.getStatus()).isEqualTo(MemberStatus.REJECTED_PENDING_CONFIRM);
            verify(saveMemberPort, times(1)).save(targetMember);
            verify(saveCrewMemberPort, times(1)).save(targetCrewMember);
        }

        @Test
        @DisplayName("이미 승인되었거나 거절된 유저를 다시 승인하려 하면 예외가 발생한다.")
        void approveJoinRequest_Fail_InvalidStatus() {
            // given
            Long crewId = 100L;
            Long leaderId = 1L;
            long targetMemberId = 2L;
            ProcessJoinCommand command = ProcessJoinCommand.of(crewId, 2L, 1L);

            // 리더 권한 설정
            CrewMember leader = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(leaderId)
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));

            // 이미 JOINED 상태인 멤버
            CrewMember alreadyJoinedMember = CrewMember.builder()
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, targetMemberId)).willReturn(Optional.of(alreadyJoinedMember));

            // when & then
            assertThatThrownBy(() -> crewService.approveJoinRequest(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("가입 요청 목록 조회 테스트")
    class GetJoinRequestsTest {

        @Test
        @DisplayName("성공: 크루 리더가 가입 신청 목록을 조회하면 신청자 리스트를 반환한다.")
        void getJoinRequests_Success() {
            // given
            Long crewId = 100L;
            Long leaderId = 1L;

            // 리더 권한 모킹
            CrewMember leader = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(leaderId)
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));

            // 가입 신청 목록 모킹
            JoinRequestDto result1 = new JoinRequestDto(2L, "러너A", "img1.png", LocalDateTime.now());
            JoinRequestDto result2 = new JoinRequestDto(3L, "러너B", "img2.png", LocalDateTime.now());
            given(loadCrewMemberPort.findJoinRequestsByCrewId(crewId)).willReturn(List.of(result1, result2));

            JoinRequestMemberResponse response1 = new JoinRequestMemberResponse(2L, "러너A", "img1.png", LocalDateTime.now());
            JoinRequestMemberResponse response2 = new JoinRequestMemberResponse(3L, "러너B", "img2.png", LocalDateTime.now());

            // any()를 사용하거나 특정 객체를 지정하여 스터빙
            given(crewMemberResponseMapper.toJoinRequestResponse(result1)).willReturn(response1);
            given(crewMemberResponseMapper.toJoinRequestResponse(result2)).willReturn(response2);

            // when
            List<JoinRequestMemberResponse> responses = crewService.getJoinRequests(crewId, leaderId);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.getFirst().nickname()).isEqualTo("러너A");
            verify(loadCrewMemberPort, times(1)).findJoinRequestsByCrewId(crewId);
        }

        @Test
        @DisplayName("실패 (권한 없음): 크루 리더가 아닌 일반 멤버가 조회를 시도하면 FORBIDDEN 에러가 발생한다.")
        void getJoinRequests_Fail_Forbidden() {
            // given
            Long crewId = 100L;
            Long memberId = 2L;

            // 일반 멤버(MEMBER)로 모킹
            CrewMember member = CrewMember.builder()
                    .crewId(crewId).memberId(memberId).role(CrewMemberRole.MEMBER).build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> crewService.getJoinRequests(crewId, memberId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 (크루 멤버 아님): 크루에 속하지 않은 사용자가 조회를 시도하면 에러가 발생한다.")
        void getJoinRequests_Fail_NotMember() {
            // given
            Long crewId = 100L;
            Long nonMemberId = 999L;

            // 가입 내역 없음
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, nonMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.getJoinRequests(crewId, nonMemberId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("초대 코드 재발급 테스트")
    class RegenerateInvitationCodeTest {

        @Test
        @DisplayName("성공: 크루 리더가 초대 코드를 재발급하면 새로운 코드가 반환되고 크루에 저장된다.")
        void regenerateInvitationCode_Success() {
            // given
            Long crewId = 100L;
            Long leaderId = 1L;
            String oldCode = "OLDCOD";

            CrewMember leader = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(leaderId)
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));

            Crew crew = Crew.builder()
                    .id(crewId)
                    .invitationCode(oldCode)
                    .build();
            given(loadCrewPort.findById(crewId)).willReturn(Optional.of(crew));

            // 새 코드는 항상 유일하다고 가정
            given(loadCrewPort.existsByInvitationCode(anyString())).willReturn(false);

            // when
            InvitationCodeResponse response = crewService.regenerateInvitationCode(crewId, leaderId);

            // then
            assertThat(response.invitationCode()).isNotBlank();
            assertThat(response.invitationCode()).isNotEqualTo(oldCode);
            verify(saveCrewPort, times(1)).save(crew);
        }

        @Test
        @DisplayName("실패 (권한 없음): 리더가 아닌 일반 멤버가 재발급을 시도하면 NOT_CREW_LEADER 예외가 발생한다.")
        void regenerateInvitationCode_Fail_NotLeader() {
            // given
            Long crewId = 100L;
            Long memberId = 2L;

            CrewMember member = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(memberId)
                    .role(CrewMemberRole.MEMBER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> crewService.regenerateInvitationCode(crewId, memberId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.NOT_CREW_LEADER.getMessage());
        }

        @Test
        @DisplayName("실패 (크루 멤버 아님): 해당 크루에 속하지 않은 사용자가 재발급을 시도하면 NOT_A_CREW_MEMBER 예외가 발생한다.")
        void regenerateInvitationCode_Fail_NotMember() {
            // given
            Long crewId = 100L;
            Long nonMemberId = 999L;

            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, nonMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.regenerateInvitationCode(crewId, nonMemberId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.NOT_A_CREW_MEMBER.getMessage());
        }

        @Test
        @DisplayName("실패 (크루 없음): 유효하지 않은 crewId로 재발급을 시도하면 CREW_NOT_FOUND 예외가 발생한다.")
        void regenerateInvitationCode_Fail_CrewNotFound() {
            // given
            Long crewId = 999L;
            Long leaderId = 1L;

            CrewMember leader = CrewMember.builder()
                    .crewId(crewId)
                    .memberId(leaderId)
                    .role(CrewMemberRole.LEADER)
                    .status(CrewMemberStatus.JOINED)
                    .build();
            given(loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)).willReturn(Optional.of(leader));
            given(loadCrewPort.findById(crewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.regenerateInvitationCode(crewId, leaderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CrewErrorCode.CREW_NOT_FOUND.getMessage());
        }
    }

}