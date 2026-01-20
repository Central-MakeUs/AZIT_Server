package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewMemberRepository;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrewMemberPersistenceAdapter implements LoadCrewMemberPort, SaveCrewMemberPort {
    private final CrewMemberRepository crewMemberRepository;
    private final CrewMemberMapper crewMemberMapper;

    @Override
    public CrewMember save(CrewMember crewMember) {
        CrewMemberEntity entity = crewMemberMapper.toEntity(crewMember);
        CrewMemberEntity savedEntity = crewMemberRepository.save(entity);
        return crewMemberMapper.toDomain(savedEntity);
    }
}
