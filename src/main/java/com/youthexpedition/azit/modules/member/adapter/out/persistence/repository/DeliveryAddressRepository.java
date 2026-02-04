package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddressEntity, Long>, DeliveryAddressRepositoryCustom {
    boolean existsByMemberId(Long memberId);
    Optional<DeliveryAddressEntity> findByMemberIdAndIsDefaultTrue(Long memberId);
}
