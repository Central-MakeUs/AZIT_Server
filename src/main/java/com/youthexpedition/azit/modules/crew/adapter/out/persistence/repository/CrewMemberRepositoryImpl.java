package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
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
    public List<JoinRequestDto> findJoinRequestsByCrewId(Long crewId) {
        return queryFactory
                .select(Projections.constructor(JoinRequestDto.class,
                        memberEntity.id,
                        memberEntity.nickname,
                        memberEntity.profileImageUrl,
                        crewMemberEntity.createdAt
                ))
                .from(crewMemberEntity)
                .join(memberEntity).on(crewMemberEntity.memberId.eq(memberEntity.id))
                .where(
                        crewMemberEntity.crew.id.eq(crewId),
                        crewMemberEntity.status.eq(CrewMemberStatus.REQUESTED) // 대기 중인 신청만 조회
                )
                .orderBy(crewMemberEntity.createdAt.asc()) // 먼저 신청한 순서대로 정렬
                .fetch();
    }

    @Override
    public SliceResponse<CrewMemberInfoDto> findAllJoinedMembersByCrewId(Long crewId, CursorPageQuery query) {
        List<CrewMemberInfoDto> content = queryFactory
                .select(Projections.constructor(CrewMemberInfoDto.class,
                        memberEntity.id,
                        memberEntity.nickname,
                        memberEntity.profileImageUrl,
                        crewMemberEntity.role,
                        crewMemberEntity.createdAt
                ))
                .from(crewMemberEntity)
                .join(memberEntity).on(crewMemberEntity.memberId.eq(memberEntity.id))
                .where(
                        crewMemberEntity.crew.id.eq(crewId),
                        crewMemberEntity.status.eq(CrewMemberStatus.JOINED),
                        ltCursorId(query.cursorId())
                )
                .orderBy(
                        crewMemberEntity.role.asc(),    // 리더 우선
                        crewMemberEntity.updatedAt.desc()      // 최신 가입 순
                )
                .limit(query.size() + 1) // 다음 페이지 확인을 위해 size + 1 조회
                .fetch();

        boolean hasNext = content.size() > query.size();
        if (hasNext) {
            content.remove(query.size());
        }

        Long lastId = content.isEmpty() ? null : content.getLast().id();
        return new SliceResponse<>(content, hasNext, lastId);
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId == null ? null : crewMemberEntity.id.lt(cursorId);
    }
}
