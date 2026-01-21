package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    @InjectMocks
    private CrewService crewService;

    @Test
    @DisplayName("크루 생성 완료 시 초대 코드를 포함한 DTO를 반환하고, 멤버 상태가 ACTIVE로 변경된다.")
    void createCrew_Service_Success() {
        // given
        Long leaderId = 1L;
        CreateCrewCommand command = CreateCrewCommand.of("아지트 러닝크루", "RUNNING", "SEOUL", leaderId);

        Crew mockCrew = Crew.builder().id(100L).invitationCode("ABC123").build();
        given(saveCrewPort.save(any(Crew.class))).willReturn(mockCrew);

        Member member = Member.builder()
                .id(leaderId)
                .status(MemberStatus.PENDING_ONBOARDING)
                .build();
        given(loadMemberPort.findById(leaderId)).willReturn(Optional.of(member));

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

    @Test
    @DisplayName("크루 가입 요청 시 초대 코드가 일치하면 REQUESTED 상태로 등록된다.")
    void joinCrew_Success() {
        // given
        Long crewId = 100L;
        Long memberId = 1L;
        String invitationCode = "ABC123";
        JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

        // 초대 코드가 포함된 크루
        Crew mockCrew = Crew.builder()
                .id(crewId)
                .invitationCode(invitationCode)
                .build();
        given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));
        given(loadCrewMemberPort.existsByCrewIdAndMemberId(crewId, memberId)).willReturn(false);

        // when
        crewService.joinCrew(command);

        // then
        verify(loadCrewPort, times(1)).findByInvitationCode(invitationCode);

        // 가입 신청자(MEMBER)가 REQUESTED 상태로 저장되었는지 확인
        verify(saveCrewMemberPort, times(1)).save(argThat(crewMember ->
                crewMember.getCrewId().equals(crewId) &&
                        crewMember.getMemberId().equals(memberId) &&
                        crewMember.getRole() == CrewMemberRole.MEMBER &&
                        crewMember.getStatus() == CrewMemberStatus.REQUESTED
        ));
    }

    @Test
    @DisplayName("크루 가입 요청 시 존재하지 않는 크루 코드면 예외가 발생한다.")
    void joinCrew_Fail_CrewNotFound() {
        // given
        Long crewId = 100L;
        String correctCode = "ABC123";
        String wrongCode = "WRONG_CODE";
        JoinCrewCommand command = JoinCrewCommand.of(1L, wrongCode);

        given(loadCrewPort.findByInvitationCode(wrongCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> crewService.joinCrew(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CrewErrorCode.CREW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 가입된 멤버(또는 리더)가 가입 신청을 하면 예외가 발생한다.")
    void joinCrew_Fail_AlreadyJoined() {
        // given
        Long memberId = 1L;
        String invitationCode = "ABC123";
        Long crewId = 100L;
        JoinCrewCommand command = JoinCrewCommand.of(memberId, invitationCode);

        Crew mockCrew = Crew.builder().id(crewId).build();
        given(loadCrewPort.findByInvitationCode(invitationCode)).willReturn(Optional.of(mockCrew));

        // 이미 멤버가 존재한다고 가정
        given(loadCrewMemberPort.existsByCrewIdAndMemberId(crewId, memberId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> crewService.joinCrew(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CrewErrorCode.ALREADY_JOINED_CREW.getMessage());

        // 가입 저장이 호출되지 않았는지 확인
        verify(saveCrewMemberPort, never()).save(any());
    }

}