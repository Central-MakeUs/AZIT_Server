package com.youthexpedition.azit.modules.member.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppleNotificationType {
    CONSENT_REVOKED("consent-revoked"), // 사용자가 앱 사용 중단
    ACCOUNT_DELETE("account-delete"),   // Apple ID 계정 삭제
    EMAIL_ENABLED("email-enabled"),     // 이메일 공유 활성화
    EMAIL_DISABLED("email-disabled");   // 이메일 공유 비활성화

    private final String type;

    public static AppleNotificationType of(String type) {
        for (AppleNotificationType notificationType : values()) {
            if (notificationType.type.equals(type)) {
                return notificationType;
            }
        }
        return null;
    }
}
