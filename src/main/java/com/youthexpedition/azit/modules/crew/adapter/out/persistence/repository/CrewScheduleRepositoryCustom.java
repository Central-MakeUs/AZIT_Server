package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CrewScheduleRepositoryCustom {
    List<CrewScheduleEntity> findAllByFilter(Long crewId, LocalDate date, YearMonth yearMonth, RunType runType);
    Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, YearMonth yearMonth);
    List<CrewScheduleEntity> findAllByMemberId(Long memberId);
    List<CrewScheduleEntity> findAllTodaySchedulesByMemberId(Long memberId, LocalDateTime now);
    Optional<CrewScheduleEntity> findNextClosestScheduleByMemberId(Long memberId, LocalDateTime now);
    List<CrewScheduleEntity> findAllByMemberIdAndMonth(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId);
    Map<LocalDate, Set<RunType>> findMyMonthlyAttendanceForCalendar(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId);
    boolean existsConflictingSchedule(Long memberId, LocalDateTime newMeetingAt, Long excludeScheduleId);
    List<CrewScheduleEntity> findSchedulesToCancelByCreator(Long crewId, Long memberId, LocalDateTime now);
    List<CrewScheduleEntity> findSchedulesToRemoveParticipant(Long crewId, Long memberId, LocalDateTime now);
    List<CrewScheduleEntity> findActiveSchedulesByCrewId(Long crewId, LocalDateTime now);
}
