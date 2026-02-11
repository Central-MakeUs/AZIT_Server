package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QOrderEntity.orderEntity;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<OrderEntity> findOrdersByMemberId(Long memberId, CursorPageQuery query) {
        // hasNext 판단을 위해 size + 1 개 조회
        List<OrderEntity> content = queryFactory
                .selectFrom(orderEntity)
                .where(
                        orderEntity.memberId.eq(memberId),
                        ltCursorId(query.cursorId())
                )
                .limit(query.size() + 1)
                .orderBy(orderEntity.id.desc())
                .fetch();

        boolean hasNext = content.size() > query.size();
        if (hasNext) {
            content.remove(query.size());
        }

        Long lastId = content.isEmpty() ? null : content.getLast().getId();
        return new SliceResponse<>(content, hasNext, lastId);
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId == null ? null : orderEntity.id.lt(cursorId);
    }
}
