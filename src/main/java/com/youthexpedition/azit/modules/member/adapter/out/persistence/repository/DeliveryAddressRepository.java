package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddressEntity, Long>, DeliveryAddressRepositoryCustom {
    boolean existsByMemberId(Long memberId);
    Optional<DeliveryAddressEntity> findByMemberIdAndIsDefaultTrue(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DeliveryAddressEntity da WHERE da.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
