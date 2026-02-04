package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.AddressEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.QAddressEntity.addressEntity;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<AddressEntity> findAllByMemberIdAndIsDefaultTrue(Long memberId) {
        return queryFactory
                .selectFrom(addressEntity)
                .where(addressEntity.memberId.eq(memberId))
                .orderBy(
                        addressEntity.isDefault.desc(), // 1순위: 기본 배송지 (true -> false 순)
                        addressEntity.createdAt.desc()  // 2순위: 최신 등록순
                )
                .fetch();
    }
}
