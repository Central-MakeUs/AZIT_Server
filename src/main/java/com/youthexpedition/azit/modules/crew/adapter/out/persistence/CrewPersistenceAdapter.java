package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewPersistenceAdapter implements LoadCrewPort, SaveCrewPort {
    private final CrewRepository crewRepository;
    private final CrewMapper crewMapper;

    @Override
    public Crew save(Crew crew) {
        CrewEntity entity = crewMapper.toEntity(crew);
        CrewEntity savedEntity = crewRepository.save(entity);
        return crewMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Crew> findById(Long id) {
        return crewRepository.findById(id).map(crewMapper::toDomain);
    }

    @Override
    public Optional<Crew> findByInvitationCode(String invitationCode) {
        return crewRepository.findByInvitationCode(invitationCode).map(crewMapper::toDomain);
    }

    @Override
    public boolean existsByInvitationCode(String invitationCode) {
        return crewRepository.existsByInvitationCode(invitationCode);
    }
}
