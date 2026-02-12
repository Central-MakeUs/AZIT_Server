package com.youthexpedition.azit.modules.member.application.port.in.command;

public record UpdateAddressCommand(
        Long memberId,
        Long addressId,
        String recipientName,
        String phoneNumber,
        String zipcode,
        String baseAddress,
        String detailAddress,
        boolean isDefault
) {
    public static UpdateAddressCommand of(Long memberId, Long addressId, String recipientName, String phoneNumber, String zipcode,
                                          String baseAddress, String detailAddress, boolean isDefault) {
        return new UpdateAddressCommand(
                memberId, addressId, recipientName, phoneNumber, zipcode, baseAddress, detailAddress, isDefault);
    }
}
