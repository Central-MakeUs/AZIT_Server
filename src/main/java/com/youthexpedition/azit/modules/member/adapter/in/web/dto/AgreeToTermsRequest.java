package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AgreeToTermsRequest(
        @Schema(description = "서비스 이용약관 동의 여부 (필수)")
        @NotNull(message = "서비스 이용약관 동의 여부는 필수입니다.")
        Boolean serviceTermsAgreed,

        @Schema(description = "개인정보 처리방침 동의 여부 (필수)")
        @NotNull(message = "개인정보 처리방침 동의 여부는 필수입니다.")
        Boolean privacyPolicyAgreed,

        @Schema(description = "위치기반 서비스 이용약관 동의 여부 (필수)")
        @NotNull(message = "위치기반 서비스 이용약관 동의 여부는 필수입니다.")
        Boolean locationServiceAgreed,

        @Schema(description = "제3자 정보제공 동의 여부 (필수)")
        @NotNull(message = "제3자 정보제공 동의 여부는 필수입니다.")
        Boolean thirdPartyInfoAgreed,

        @Schema(description = "마케팅 정보 수신 동의 여부 (선택)")
        @NotNull(message = "마케팅 정보 수신 동의 여부는 필수입니다.")
        Boolean marketingTermsAgreed,

        @Schema(description = "알림 수신 동의 여부 (선택)")
//        @NotNull(message = "알림 수신 동의 여부는 필수입니다.")
        Boolean notificationTermsAgreed
) {
    public AgreeToTermsCommand toCommand() {
        return AgreeToTermsCommand.of(
                Boolean.TRUE.equals(serviceTermsAgreed),
                Boolean.TRUE.equals(privacyPolicyAgreed),
                Boolean.TRUE.equals(locationServiceAgreed),
                Boolean.TRUE.equals(thirdPartyInfoAgreed),
                Boolean.TRUE.equals(marketingTermsAgreed),
                Boolean.TRUE.equals(notificationTermsAgreed)
        );
    }
}
