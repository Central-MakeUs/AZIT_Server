package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.mapper.AddressMapper;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.AddressEntity;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.AddressRepository;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddressPersistenceAdapter implements LoadAddressPort, SaveAddressPort {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public void save(Address address) {
        AddressEntity entity = addressMapper.toEntity(address);
        addressRepository.save(entity);
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        return addressRepository.existsByMemberId(memberId);
    }

    @Override
    public Optional<Address> findDefaultByMemberId(Long memberId) {
        return addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .map(addressMapper::toDomain);
    }

    @Override
    public Optional<Address> findById(Long addressId) {
        return addressRepository.findById(addressId)
                .map(addressMapper::toDomain);
    }
}
