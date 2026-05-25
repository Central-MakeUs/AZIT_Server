package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateCrewProfileCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInfoResponse;

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
    void cancelJoinRequest(Long crewId, Long memberId);
    void exitCrew(Long crewId, Long memberId);
    InvitationCodeResponse regenerateInvitationCode(Long crewId, Long memberId);
    void updateCrewProfile(Long crewId, Long memberId, UpdateCrewProfileCommand command);
    CrewInfoResponse getCrewInfo(Long crewId, Long memberId);
    void dissolveCrew(Long crewId, Long leaderId);
}
