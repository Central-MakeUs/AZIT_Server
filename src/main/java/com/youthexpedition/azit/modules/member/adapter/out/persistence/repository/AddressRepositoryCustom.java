package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.AddressEntity;

import java.util.List;

public interface AddressRepositoryCustom {
    List<AddressEntity> findAllByMemberIdAndIsDefaultTrue(Long memberId);
}
