package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AgreeToTermsRequest(
        @Schema(description = "서비스 이용약관 동의 여부 (필수)", example = "true")
        @NotNull(message = "서비스 이용약관 동의 여부는 필수입니다.")
        boolean serviceTermsAgreed,

        @Schema(description = "개인정보 처리방침 동의 여부 (필수)", example = "true")
        @NotNull(message = "개인정보 처리방침 동의 여부는 필수입니다.")
        boolean privacyPolicyAgreed,

        @Schema(description = "위치기반 서비스 이용약관 동의 여부 (필수)", example = "true")
        @NotNull(message = "위치기반 서비스 이용약관 동의 여부는 필수입니다.")
        boolean locationServiceAgreed,

        @Schema(description = "제3자 정보제공 동의 여부 (필수)", example = "true")
        @NotNull(message = "제3자 정보제공 동의 여부는 필수입니다.")
        boolean thirdPartyInfoAgreed,

        @Schema(description = "마케팅 정보 수신 동의 여부 (선택)", example = "false")
        @NotNull(message = "마케팅 정보 수신 동의 여부는 필수입니다.") // 값 자체는 반드시 전송되어야 함
        boolean marketingTermsAgreed
) {
    public AgreeToTermsCommand toCommand() {
        return AgreeToTermsCommand.of(
                serviceTermsAgreed,
                privacyPolicyAgreed,
                locationServiceAgreed,
                thirdPartyInfoAgreed,
                marketingTermsAgreed
        );
    }
}
