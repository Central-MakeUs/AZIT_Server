package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record LinkedProviderResponse(
        @Schema(description = "연동된 소셜 로그인 목록")
        List<SocialProvider> providers
) {
    public static LinkedProviderResponse of(List<SocialProvider> providers) {
        return new LinkedProviderResponse(providers);
    }
}
