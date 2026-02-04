package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterAddressRequest(
        @Schema(description = "수령인")
        @NotBlank(message = "수령인은 필수입니다.")
        String recipientName,

        @Schema(description = "연락처")
        @NotBlank(message = "연락처는 필수입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 연락처 형식이 아닙니다.")
        String phoneNumber,

        @Schema(description = "우편번호")
        @NotBlank(message = "우편번호는 필수입니다.")
        String zipcode,

        @Schema(description = "주소")
        @NotBlank(message = "주소는 필수입니다.")
        String baseAddress,

        @Schema(description = "상세 주소")
        @NotBlank(message = "상세 주소는 필수입니다.")
        String detailAddress,

        @Schema(description = "기본 배송지 여부")
        @NotNull(message = "기본 배송지 여부는 필수입니다.")
        boolean isDefault
) {
    public RegisterAddressCommand toCommand(Long memberId) {
        return new RegisterAddressCommand(memberId, recipientName, phoneNumber, zipcode, baseAddress, detailAddress, isDefault);
    }
}
