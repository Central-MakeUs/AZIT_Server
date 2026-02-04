package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;

import java.util.List;

public interface DeliveryAddressRepositoryCustom {
    List<DeliveryAddressEntity> findAllByMemberIdAndIsDefaultTrue(Long memberId);
}
