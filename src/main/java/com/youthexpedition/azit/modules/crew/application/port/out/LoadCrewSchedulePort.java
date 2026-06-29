package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LoadCrewSchedulePort {
    Optional<CrewSchedule> findById(Long scheduleId);
    List<CrewSchedule> findAllByFilter(Long crewId, LocalDate date, LocalDate startDate, LocalDate endDate, YearMonth yearMonth, RunType runType);
    Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, LocalDate startDate, LocalDate endDate, YearMonth yearMonth);
    List<CrewSchedule> findAllByMemberId(Long memberId);
    List<CrewSchedule> findAllTodaySchedulesByMemberId(Long memberId, LocalDateTime now);
    Optional<CrewSchedule> findNextClosestScheduleByMemberId(Long memberId, LocalDateTime now);
    List<CrewSchedule> findAllByMemberIdAndMonth(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId);
    Map<LocalDate, Set<RunType>> findMyMonthlyAttendanceForCalendar(Long memberId, YearMonth yearMonth, LocalDateTime now, Long crewId);
    boolean existsConflictingSchedule(Long memberId, LocalDateTime newMeetingAt, Long excludeScheduleId);
    List<CrewSchedule> findSchedulesToCancel(Long crewId, Long memberId, LocalDateTime now);
    List<CrewSchedule> findSchedulesToRemoveParticipant(Long crewId, Long memberId, LocalDateTime now);
    List<CrewSchedule> findActiveSchedulesByCrewId(Long crewId, LocalDateTime now);
}
