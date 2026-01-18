package com.youthexpedition.azit.modules.auth.application.port.out;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialLoginCommand;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.domain.model.SocialProfile;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary // 동일 타입 빈 중 최우선 주입
@Component
@RequiredArgsConstructor
public class SocialAuthComposite implements SocialAuthPort {
    private final List<SocialAuthPort> socialAuthPorts;

    @Override
    public SocialProfile getSocialProfile(SocialLoginCommand command) {
        return getAdapter(command.socialProvider())
                .getSocialProfile(command);
    }

    @Override
    public void revoke(SocialRevokeCommand command) {
        getAdapter(command.provider()).revoke(command);
    }

    @Override
    public SocialProvider getProvider() {
        return null; // Composite 자체는 특정 프로바이더를 갖지 않음
    }

    private SocialAuthPort getAdapter(SocialProvider provider) {
        return socialAuthPorts.stream()
                .filter(port -> port.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_SOCIAL_PROVIDER));
    }
}
