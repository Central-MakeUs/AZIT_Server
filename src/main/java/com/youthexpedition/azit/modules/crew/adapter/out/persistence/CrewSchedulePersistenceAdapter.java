package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewScheduleMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewScheduleRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CrewSchedulePersistenceAdapter implements LoadCrewSchedulePort, SaveCrewSchedulePort {
    private final CrewScheduleRepository crewScheduleRepository;
    private final CrewScheduleMapper crewScheduleMapper;

    @Override
    public void save(CrewSchedule crewSchedule) {
        if (crewSchedule.getId() != null) {
            // 수정할 경우 기존 엔티티를 영속성 컨텍스트로 가져와서 업데이트
            crewScheduleRepository.findByIdWithDetails(crewSchedule.getId())
                    .ifPresent(entity -> crewScheduleMapper.updateEntity(entity, crewSchedule));
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

    @Override
    public List<CrewSchedule> findAllByFilter(Long crewId, LocalDate date, RunType runType) {
        return crewScheduleRepository.findAllByFilter(crewId, date, runType).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }

    @Override
    public Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, YearMonth yearMonth) {
        return crewScheduleRepository.findMonthlySchedulesForCalendar(crewId, yearMonth);
    }

    @Override
    public List<CrewSchedule> findAllByMemberId(Long memberId) {
        return crewScheduleRepository.findAllByMemberId(memberId).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }
}
