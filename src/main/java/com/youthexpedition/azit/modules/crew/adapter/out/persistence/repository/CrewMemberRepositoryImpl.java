package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.crew.application.port.out.model.JoinRequestQueryResult;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.QCrewMemberEntity.crewMemberEntity;
import static com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.QMemberEntity.memberEntity;

@Repository
@RequiredArgsConstructor
public class CrewMemberRepositoryImpl implements CrewMemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<JoinRequestQueryResult> findJoinRequestsByCrewId(Long crewId) {
        return queryFactory
                .select(Projections.constructor(JoinRequestQueryResult.class,
                        memberEntity.id,
                        memberEntity.nickname,
                        memberEntity.profileImageUrl,
                        crewMemberEntity.createdAt
                ))
                .from(crewMemberEntity)
                .join(memberEntity).on(crewMemberEntity.memberId.eq(memberEntity.id))
                .where(
                        crewMemberEntity.crewId.eq(crewId),
                        crewMemberEntity.status.eq(CrewMemberStatus.REQUESTED) // 대기 중인 신청만 조회
                )
                .orderBy(crewMemberEntity.createdAt.asc()) // 먼저 신청한 순서대로 정렬
                .fetch();
    }
}
