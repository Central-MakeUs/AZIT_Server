package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public List<Crew> findAllByIds(List<Long> ids) {
        return crewRepository.findAllById(ids).stream()
                .map(crewMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Crew> crews) {
        List<CrewEntity> entities = crews.stream()
                .map(crewMapper::toEntity)
                .toList();
        crewRepository.saveAll(entities);
    }

    @Override
    public void incrementMemberCount(Long crewId) {
        crewRepository.incrementMemberCount(crewId);
    }

    @Override
    public void decrementMemberCount(Long crewId) {
        crewRepository.decrementMemberCount(crewId);
    }

    @Override
    public void decrementMemberCountBatch(List<Long> crewIds) {
        if (crewIds == null || crewIds.isEmpty()) return;
        crewRepository.decrementMemberCountBatch(crewIds);
    }
}
