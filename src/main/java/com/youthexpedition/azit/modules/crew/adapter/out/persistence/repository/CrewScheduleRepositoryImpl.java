package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.QCrewScheduleEntity.crewScheduleEntity;

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
