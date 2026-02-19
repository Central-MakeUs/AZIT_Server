package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewScheduleMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewScheduleRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewSchedulePersistenceAdapter implements LoadCrewSchedulePort, SaveCrewSchedulePort {
    private final CrewScheduleRepository crewScheduleRepository;
    private final CrewScheduleMapper crewScheduleMapper;

    @Override
    public void save(CrewSchedule crewSchedule) {
        if (crewSchedule.getId() != null) {
            // 수정할 경우 기존 엔티티를 영속성 컨텍스트로 가져와서 업데이트
            crewScheduleRepository.findByIdWithDetails(crewSchedule.getId()).ifPresent(entity -> {
                crewScheduleMapper.updateEntity(entity, crewSchedule);
            });
        } else {
            // 생성할 경우
            CrewScheduleEntity entity = crewScheduleMapper.toEntity(crewSchedule);
            crewScheduleRepository.save(entity);
        }
    }

    @Override
    public Optional<CrewSchedule> findById(Long scheduleId) {
        return crewScheduleRepository.findByIdWithDetails(scheduleId)
                .map(crewScheduleMapper::toDomain);
    }
}
