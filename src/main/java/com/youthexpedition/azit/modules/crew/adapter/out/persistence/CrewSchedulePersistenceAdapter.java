package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewScheduleMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewScheduleRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrewSchedulePersistenceAdapter implements LoadCrewSchedulePort, SaveCrewSchedulePort {
    private final CrewScheduleRepository crewScheduleRepository;
    private final CrewScheduleMapper crewScheduleMapper;

    @Override
    public void save(CrewSchedule crewSchedule) {
        CrewScheduleEntity entity = crewScheduleMapper.toEntity(crewSchedule);
        crewScheduleRepository.save(entity);
    }
}
