package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @Mock
    private SaveCrewPort saveCrewPort;
    @Mock
    private SaveCrewMemberPort saveCrewMemberPort;
    @Mock
    private SaveMemberPort saveMemberPort;
    @Mock
    private LoadMemberPort loadMemberPort;
    @InjectMocks
    private CrewService crewService;

    @Test
    @DisplayName("크루 생성 완료 시 초대 코드를 포함한 DTO를 반환하고, 멤버 상태가 ACTIVE로 변경됨")
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

}