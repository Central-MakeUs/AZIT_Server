package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewService implements CrewUseCase {
    private final SaveCrewPort saveCrewPort;
    private final SaveCrewMemberPort saveCrewMemberPort;

    @Override
    public String createCrew(CreateCrewCommand command) {
        // 크루 생성
        Crew crew = Crew.create(command.name(), command.category(), command.region());
        Crew savedCrew = saveCrewPort.save(crew);

        // 리더 등록
        CrewMember leader = CrewMember.createAsLeader(savedCrew.getId(), command.leaderId());
        saveCrewMemberPort.save(leader);

        return savedCrew.getInvitationCode();
    }
}
