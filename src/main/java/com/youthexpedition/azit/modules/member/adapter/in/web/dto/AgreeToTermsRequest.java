package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import jakarta.validation.constraints.NotNull;

public record AgreeToTermsRequest(
        @NotNull boolean serviceTermsAgreed,
        @NotNull boolean privacyPolicyAgreed,
        @NotNull boolean locationServiceAgreed,
        @NotNull boolean thirdPartyInfoAgreed,
        @NotNull boolean marketingTermsAgreed
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
