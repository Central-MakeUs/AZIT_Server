package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.QDeliveryAddressEntity.deliveryAddressEntity;


@Repository
@RequiredArgsConstructor
public class DeliveryAddressRepositoryImpl implements DeliveryAddressRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<DeliveryAddressEntity> findAllByMemberIdAndIsDefaultTrue(Long memberId) {
        return queryFactory
                .selectFrom(deliveryAddressEntity)
                .where(deliveryAddressEntity.memberId.eq(memberId))
                .orderBy(
                        deliveryAddressEntity.isDefault.desc(), // 1순위: 기본 배송지 (true -> false 순)
                        deliveryAddressEntity.createdAt.desc()  // 2순위: 최신 등록순
                )
                .fetch();
    }
}
