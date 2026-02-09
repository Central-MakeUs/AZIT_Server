package com.youthexpedition.azit.modules.member.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeliveryAddressResponse(
        @Schema(description = "배송지 ID")
        Long id,
        @Schema(description = "수령인 성함")
        String recipientName,
        @Schema(description = "수령인 연락처")
        String phoneNumber,
        @Schema(description = "우편번호")
        String zipcode,
        @Schema(description = "기본 주소")
        String baseAddress,
        @Schema(description = "상세 주소")
        String detailAddress,
        @Schema(description = "기본 배송지 여부")
        boolean isDefault
) {
    public static DeliveryAddressResponse of(Long id, String recipientName, String phoneNumber, String zipcode, String baseAddress,
                                             String detailAddress, boolean isDefault) {
        return new DeliveryAddressResponse(id, recipientName, phoneNumber, zipcode, baseAddress, detailAddress, isDefault);
    }
}