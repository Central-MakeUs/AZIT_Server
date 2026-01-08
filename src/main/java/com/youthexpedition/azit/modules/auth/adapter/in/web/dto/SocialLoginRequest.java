package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest (
        @NotBlank(message = "인가코드는 필수입니다.")
        String authorizationCode
){
    public SocialLoginCommand toCommand(SocialProvider provider) {
        return SocialLoginCommand.of(provider, this.authorizationCode);
    }
}
