package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.mapper.DeliveryAddressMapper;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.DeliveryAddressRepository;
import com.youthexpedition.azit.modules.member.application.port.out.LoadDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeliveryAddressPersistenceAdapter implements LoadDeliveryAddressPort, SaveDeliveryAddressPort {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;

    @Override
    public void save(DeliveryAddress deliveryAddress) {
        DeliveryAddressEntity entity = deliveryAddressMapper.toEntity(deliveryAddress);
        deliveryAddressRepository.save(entity);
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        return deliveryAddressRepository.existsByMemberId(memberId);
    }

    @Override
    public Optional<DeliveryAddress> findDefaultByMemberId(Long memberId) {
        return deliveryAddressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .map(deliveryAddressMapper::toDomain);
    }

    @Override
    public Optional<DeliveryAddress> findById(Long addressId) {
        return deliveryAddressRepository.findById(addressId)
                .map(deliveryAddressMapper::toDomain);
    }

    @Override
    public void delete(DeliveryAddress deliveryAddress) {
        deliveryAddressRepository.deleteById(deliveryAddress.getId());
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        deliveryAddressRepository.deleteByMemberId(memberId);
    }

    @Override
    public List<DeliveryAddress> findAllByMemberIdOrderByDefaultDescCreatedAtDesc(Long memberId) {
        return deliveryAddressRepository.findAllByMemberIdOrderByDefaultDescCreatedAtDesc(memberId)
                .stream()
                .map(deliveryAddressMapper::toDomain)
                .toList();
    }
}
