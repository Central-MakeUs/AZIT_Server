package com.youthexpedition.azit.modules.member.application.port.in.command;

public record RegisterAddressCommand(
        Long memberId,
        String recipientName,
        String phoneNumber,
        String zipcode,
        String baseAddress,
        String detailAddress,
        boolean isDefault
) {
    public static RegisterAddressCommand of(Long memberId, String recipientName, String phoneNumber, String zipcode,
            String baseAddress, String detailAddress, boolean isDefault) {
        return new RegisterAddressCommand(
                memberId, recipientName, phoneNumber, zipcode, baseAddress, detailAddress, isDefault);
    }
}
