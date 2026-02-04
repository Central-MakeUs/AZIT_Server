package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.AddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressResponseMapper {

    public List<AddressResponse> toAddressResponseList(List<Address> addresses) {
        return addresses.stream()
                .map(this::toAddressResponse)
                .toList();
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getZipcode(),
                address.getBaseAddress(),
                address.getDetailAddress(),
                address.isDefault()
        );
    }
}