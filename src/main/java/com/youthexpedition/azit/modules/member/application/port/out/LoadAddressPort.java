package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.Address;

import java.util.List;
import java.util.Optional;

public interface LoadAddressPort {
    boolean existsByMemberId(Long memberId);
    Optional<Address> findDefaultByMemberId(Long memberId);
    Optional<Address> findById(Long addressId);
    List<Address> findAllByMemberIdOrderByDefault(Long memberId);
}
