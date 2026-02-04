package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;

import java.util.List;
import java.util.Optional;

public interface LoadDeliveryAddressPort {
    boolean existsByMemberId(Long memberId);
    Optional<DeliveryAddress> findDefaultByMemberId(Long memberId);
    Optional<DeliveryAddress> findById(Long addressId);
    List<DeliveryAddress> findAllByMemberIdOrderByDefault(Long memberId);
}
