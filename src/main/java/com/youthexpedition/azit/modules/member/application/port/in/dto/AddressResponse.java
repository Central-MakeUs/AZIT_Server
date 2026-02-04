package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.youthexpedition.azit.modules.member.domain.model.Address;

public record AddressResponse(
        Long id,
        String recipientName,
        String phoneNumber,
        String zipcode,
        String baseAddress,
        String detailAddress,
        boolean isDefault
) {
    public static AddressResponse from(Address address) {
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