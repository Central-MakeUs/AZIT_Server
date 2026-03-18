package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.modules.auth.adapter.out.external.AppleAuthAdapter;
import com.youthexpedition.azit.modules.auth.application.port.in.AppleNotificationUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import com.youthexpedition.azit.modules.member.domain.model.enums.AppleNotificationType;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleNotificationService implements AppleNotificationUseCase {
    private final AppleAuthAdapter appleAuthAdapter;
    private final MemberUseCase memberUseCase;

    @Transactional
    @Override
    public void handleNotification(String payload) {
        var event = appleAuthAdapter.parseNotification(payload);
        log.debug("수신한 payload 내용: {}", event);
        AppleNotificationType type = AppleNotificationType.of(event.type());

        if (type == null) {
            log.warn("알 수 없는 Apple 알림 타입입니다: {}", event.type());
            return;
        }

        log.info("Apple 알림을 수신했습니다. Type: {}, sub: {}", type, event.sub());

        switch (type) {
            case CONSENT_REVOKED:
            case ACCOUNT_DELETE:
                // 사용자가 연동을 해제하거나 계정을 삭제한 경우 탈퇴 처리
                memberUseCase.withdrawBySocialInfo(event.sub(), SocialProvider.APPLE);
                break;

            case EMAIL_ENABLED:
                // 사용자가 이메일 공유를 다시 활성화한 경우
                memberUseCase.updateEmailSharingStatus(event.sub(), SocialProvider.APPLE, true);
                break;

            case EMAIL_DISABLED:
                // 사용자가 이메일 공유를 중단한 경우
                memberUseCase.updateEmailSharingStatus(event.sub(), SocialProvider.APPLE, false);
                break;
        }
    }
}
