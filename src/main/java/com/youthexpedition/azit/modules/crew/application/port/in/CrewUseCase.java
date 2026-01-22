package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInvitationResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewJoinStatusResponse;

public interface CrewUseCase {
    CreateCrewResponse createCrew(CreateCrewCommand command);
    void joinCrew(JoinCrewCommand command);
    CrewInvitationResponse getCrewInfoByInvitationCode(String invitationCode);
    CrewJoinStatusResponse getCrewJoinStatus(Long crewId, Long memberId);
}
