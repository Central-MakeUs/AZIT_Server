package com.youthexpedition.azit.modules.member.application.port.in.command;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;

public record AgreeToTermsCommand(
        boolean serviceTermsAgreed,      // 서비스 이용약관 (필수)
        boolean privacyPolicyAgreed,     // 개인정보 처리방침 (필수)
        boolean locationServiceAgreed,   // 위치기반 서비스 이용약관 (필수)
        boolean thirdPartyInfoAgreed,    // 제3자 정보제공 동의 (필수)
        boolean marketingTermsAgreed,     // 마케팅 정보 수신 동의 (선택)
        boolean notificationTermsAgreed   // 알림 수신 동의 (선택)
) {
    public static AgreeToTermsCommand of(
            boolean serviceTermsAgreed, boolean privacyPolicyAgreed, boolean locationServiceAgreed,
            boolean thirdPartyInfoAgreed, boolean marketingTermsAgreed, boolean notificationTermsAgreed) {
        return new AgreeToTermsCommand(
                serviceTermsAgreed, privacyPolicyAgreed, locationServiceAgreed, thirdPartyInfoAgreed, marketingTermsAgreed, notificationTermsAgreed);
    }

    // 모든 필수 약관에 동의했는지 검증
    public void validateRequired() {
//        if (!serviceTermsAgreed || !privacyPolicyAgreed || !locationServiceAgreed || !thirdPartyInfoAgreed) {
//            throw new BusinessException(MemberErrorCode.REQUIRED_TERMS_NOT_AGREED);
//        }
        if (!serviceTermsAgreed || !privacyPolicyAgreed || !thirdPartyInfoAgreed) {
            throw new BusinessException(MemberErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }
}
