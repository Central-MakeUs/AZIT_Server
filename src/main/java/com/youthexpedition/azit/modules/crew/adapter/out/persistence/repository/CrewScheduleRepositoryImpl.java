package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.QCrewScheduleEntity.crewScheduleEntity;
import static com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.QCrewScheduleMemberEntity.crewScheduleMemberEntity;

@Repository
@RequiredArgsConstructor
public class CrewScheduleRepositoryImpl implements CrewScheduleRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CrewScheduleEntity> findAllByFilter(Long crewId, LocalDate date, RunType runType) {
        return queryFactory.selectFrom(crewScheduleEntity)
                .where(
                        crewScheduleEntity.crewId.eq(crewId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE), // 삭제된 일정은 제외
                        eqDate(date),
                        eqRunType(runType)
                )
                .orderBy(crewScheduleEntity.meetingAt.asc())
                .fetch();
    }

    @Override
    public Map<LocalDate, Set<RunType>> findMonthlySchedulesForCalendar(Long crewId, YearMonth yearMonth) {
        // 해당 월의 검색 범위 계산
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 특정 컬럼만 가져오므로 tuple 사용
        List<Tuple> results = queryFactory
                .select(crewScheduleEntity.meetingAt, crewScheduleEntity.runType)
                .from(crewScheduleEntity)
                .where(
                        crewScheduleEntity.crewId.eq(crewId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE),
                        crewScheduleEntity.meetingAt.between(start, end)
                )
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(crewScheduleEntity.meetingAt)).toLocalDate(),
                        Collectors.mapping(tuple -> tuple.get(crewScheduleEntity.runType), Collectors.toSet()) // 해당 타입이 존재하는지만 알면 되므로 set
                ));
    }

    @Override
    public List<CrewScheduleEntity> findAllByMemberId(Long memberId) {
        return queryFactory.selectFrom(crewScheduleEntity)
                .join(crewScheduleEntity.members, crewScheduleMemberEntity)
                .where(
                        crewScheduleEntity.members.any().memberId.eq(memberId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE), // 취소된 일정 제외
                        crewScheduleEntity.meetingAt.goe(LocalDateTime.now()) // 지난 일정 제외
                )
                .orderBy(crewScheduleEntity.meetingAt.asc()) // 가장 가까운 순서대로 정렬
                .fetch();
    }

    @Override
    public List<CrewScheduleEntity> findAllTodaySchedulesByMemberId(Long memberId, LocalDateTime now) {
        return queryFactory.selectFrom(crewScheduleEntity)
                .join(crewScheduleEntity.members, crewScheduleMemberEntity)
                .where(
                        crewScheduleEntity.members.any().memberId.eq(memberId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE),
                        // 오늘 00:00:00 ~ 23:59:59 사이
                        crewScheduleEntity.meetingAt.between(now.with(LocalTime.MIN), now.with(LocalTime.MAX))
                )
                .orderBy(crewScheduleEntity.meetingAt.asc())
                .fetch();
    }

    @Override
    public Optional<CrewScheduleEntity> findNextClosestScheduleByMemberId(Long memberId, LocalDateTime now) {
        return Optional.ofNullable(
                queryFactory.selectFrom(crewScheduleEntity)
                        .join(crewScheduleEntity.members, crewScheduleMemberEntity)
                        .where(
                                crewScheduleEntity.members.any().memberId.eq(memberId),
                                crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE),
                                crewScheduleEntity.meetingAt.gt(now.with(LocalTime.MAX)) // 오늘 이후
                        )
                        .orderBy(crewScheduleEntity.meetingAt.asc())
                        .fetchFirst() // 첫번째 일정만 조회
        );
    }

    @Override
    public List<CrewScheduleEntity> findAllByCrewIdAndMemberId(Long crewId, Long memberId) {
        return queryFactory.selectFrom(crewScheduleEntity)
                .where(
                        crewScheduleEntity.crewId.eq(crewId),
                        crewScheduleEntity.members.any().memberId.eq(memberId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE)
                )
                .fetch();
    }

    @Override
    public List<CrewScheduleEntity> findAllByMemberIdAndMonth(Long memberId, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        return queryFactory.selectFrom(crewScheduleEntity)
                .join(crewScheduleEntity.members, crewScheduleMemberEntity)
                .where(
                        crewScheduleMemberEntity.memberId.eq(memberId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE), // 삭제된 일정 제외
                        crewScheduleEntity.meetingAt.between(start, end),
                        crewScheduleEntity.meetingAt.before(now) // 과거의 일정 또는 미리 출석한 일정
                                .or(crewScheduleMemberEntity.checkedInAt.isNotNull())
                )
                .orderBy(crewScheduleEntity.meetingAt.desc()) // 최신순 정렬
                .fetch();
    }

    @Override
    public Map<LocalDate, Set<RunType>> findMyMonthlyAttendanceForCalendar(Long memberId, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        List<Tuple> results = queryFactory
                .select(crewScheduleEntity.meetingAt, crewScheduleEntity.runType)
                .from(crewScheduleEntity)
                .join(crewScheduleEntity.members, crewScheduleMemberEntity)
                .where(
                        crewScheduleMemberEntity.memberId.eq(memberId),
                        crewScheduleEntity.status.eq(ScheduleStatus.ACTIVE),
                        crewScheduleEntity.meetingAt.between(start, end),
                        crewScheduleEntity.meetingAt.before(now)
                                .or(crewScheduleMemberEntity.checkedInAt.isNotNull())
                )
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(crewScheduleEntity.meetingAt)).toLocalDate(),
                        Collectors.mapping(tuple -> tuple.get(crewScheduleEntity.runType), Collectors.toSet())
                ));
    }

    private BooleanExpression eqDate(LocalDate date) {
        if (date == null) return null;
        // LocalDateTime의 시작(00:00:00)과 끝(23:59:59) 사이 조회
        return crewScheduleEntity.meetingAt.between(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );
    }

    private BooleanExpression eqRunType(RunType runType) {
        return runType != null ? crewScheduleEntity.runType.eq(runType) : null;
    }

}
