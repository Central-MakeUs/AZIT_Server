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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<CrewSchedule> findAllByFilter(Long crewId, LocalDate date, YearMonth yearMonth, RunType runType) {
        return crewScheduleRepository.findAllByFilter(crewId, date, yearMonth, runType).stream()
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

    @Override
    public List<CrewSchedule> findAllTodaySchedulesByMemberId(Long memberId, LocalDateTime now) {
        return crewScheduleRepository.findAllTodaySchedulesByMemberId(memberId, now).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CrewSchedule> findNextClosestScheduleByMemberId(Long memberId, LocalDateTime now) {
        return crewScheduleRepository.findNextClosestScheduleByMemberId(memberId, now)
                .map(crewScheduleMapper::toDomain);
    }

    @Override
    public void saveAll(List<CrewSchedule> crewSchedules) {
        List<Long> ids = crewSchedules.stream()
                .map(CrewSchedule::getId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) return;

        Map<Long, CrewScheduleEntity> entityMap = crewScheduleRepository.findAllByIdsWithDetails(ids).stream()
                .collect(Collectors.toMap(CrewScheduleEntity::getId, Function.identity()));

        crewSchedules.forEach(domain -> {
            CrewScheduleEntity entity = entityMap.get(domain.getId());
            if (entity != null) {
                crewScheduleMapper.updateEntity(entity, domain);
            }
        });
    }

    @Override
    public List<CrewSchedule> findAllByMemberIdAndMonth(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId) {
        return crewScheduleRepository.findAllByMemberIdAndMonth(memberId, yearMonth, now, crewId).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }

    @Override
    public Map<LocalDate, Set<RunType>> findMyMonthlyAttendanceForCalendar(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId) {
        return crewScheduleRepository.findMyMonthlyAttendanceForCalendar(memberId, yearMonth, now, crewId);
    }

    @Override
    public boolean existsConflictingSchedule(Long memberId, LocalDateTime newMeetingAt, Long excludeScheduleId) {
        return crewScheduleRepository.existsConflictingSchedule(memberId, newMeetingAt, excludeScheduleId);
    }

    @Override
    public List<CrewSchedule> findSchedulesToCancel(Long crewId, Long memberId, LocalDateTime now) {
        return crewScheduleRepository.findSchedulesToCancelByCreator(crewId, memberId, now).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }

    @Override
    public List<CrewSchedule> findSchedulesToRemoveParticipant(Long crewId, Long memberId, LocalDateTime now) {
        return crewScheduleRepository.findSchedulesToRemoveParticipant(crewId, memberId, now).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }

    @Override
    public List<CrewSchedule> findActiveSchedulesByCrewId(Long crewId, LocalDateTime now) {
        return crewScheduleRepository.findActiveSchedulesByCrewId(crewId, now).stream()
                .map(crewScheduleMapper::toDomain)
                .toList();
    }
}
