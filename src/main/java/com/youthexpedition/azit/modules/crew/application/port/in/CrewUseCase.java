package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;

import java.util.List;

public interface CrewUseCase {
    CreateCrewResponse createCrew(CreateCrewCommand command);
    void joinCrew(JoinCrewCommand command);
    CrewInvitationResponse getCrewInfoByInvitationCode(String invitationCode);
    CrewJoinStatusResponse getCrewJoinStatus(Long crewId, Long memberId);
    void approveJoinRequest(ProcessJoinCommand command);
    void rejectJoinRequest(ProcessJoinCommand command);
    List<JoinRequestMemberResponse> getJoinRequests(Long crewId, Long memberId);
    CrewMemberListResponse getCrewMembers(Long crewId, Long memberId, CursorPageQuery query);
    void expelCrewMember(Long crewId, Long leaderId, Long targetMemberId);
}
