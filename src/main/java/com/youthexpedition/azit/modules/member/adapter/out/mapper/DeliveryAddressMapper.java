package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.DeliveryAddressEntity;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberEntity;
import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import org.springframework.stereotype.Component;

@Component
public class DeliveryAddressMapper {
    public DeliveryAddress toDomain(DeliveryAddressEntity entity) {
        if (entity == null) return null;

        return DeliveryAddress.builder()
                .id(entity.getId())
                .memberId(entity.getMember().getId())
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

    public DeliveryAddressEntity toEntity(DeliveryAddress domain) {
        if (domain == null) return null;

        return DeliveryAddressEntity.builder()
                .id(domain.getId())
                .member(MemberEntity.builder().id(domain.getMemberId()).build())
                .recipientName(domain.getRecipientName())
                .phoneNumber(domain.getPhoneNumber())
                .zipcode(domain.getZipcode())
                .baseAddress(domain.getBaseAddress())
                .detailAddress(domain.getDetailAddress())
                .isDefault(domain.isDefault())
                .build();
    }
}
