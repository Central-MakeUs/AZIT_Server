package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
