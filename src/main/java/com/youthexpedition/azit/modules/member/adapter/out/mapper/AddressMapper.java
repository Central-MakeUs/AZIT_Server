package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.AddressEntity;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public Address toDomain(AddressEntity entity) {
        if (entity == null) return null;

        return Address.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .recipientName(entity.getRecipientName())
                .phoneNumber(entity.getPhoneNumber())
                .zipcode(entity.getZipcode())
                .baseAddress(entity.getBaseAddress())
                .detailAddress(entity.getDetailAddress())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AddressEntity toEntity(Address domain) {
        return AddressEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .recipientName(domain.getRecipientName())
                .phoneNumber(domain.getPhoneNumber())
                .zipcode(domain.getZipcode())
                .baseAddress(domain.getBaseAddress())
                .detailAddress(domain.getDetailAddress())
                .isDefault(domain.isDefault())
                .build();
    }
}
