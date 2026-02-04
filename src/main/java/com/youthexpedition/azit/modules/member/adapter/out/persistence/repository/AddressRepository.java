package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long>, AddressRepositoryCustom {
    boolean existsByMemberId(Long memberId);
    Optional<AddressEntity> findByMemberIdAndIsDefaultTrue(Long memberId);
}
