package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryAddressResponseMapper {

    public List<DeliveryAddressResponse> toAddressResponseList(List<DeliveryAddress> deliveryAddresses) {
        return deliveryAddresses.stream()
                .map(this::toAddressResponse)
                .toList();
    }

    private DeliveryAddressResponse toAddressResponse(DeliveryAddress deliveryAddress) {
        return new DeliveryAddressResponse(
                deliveryAddress.getId(),
                deliveryAddress.getRecipientName(),
                deliveryAddress.getPhoneNumber(),
                deliveryAddress.getZipcode(),
                deliveryAddress.getBaseAddress(),
                deliveryAddress.getDetailAddress(),
                deliveryAddress.isDefault()
        );
    }
}