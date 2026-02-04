package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송지 응답 정보")
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
    public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
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