package com.youthexpedition.azit.modules.member.domain.model.enums;

public enum TermsType {
    SERVICE,       // 서비스 이용약관 (필수)
    PRIVACY,       // 개인정보 처리방침 (필수)
    LOCATION,      // 위치기반 서비스 이용약관 (필수)
    THIRD_PARTY,   // 제3자 정보제공 동의 (필수)
    MARKETING,     // 마케팅 정보 수신 동의 (선택)
    NOTIFICATION   // 알림 수신 동의 (선택)
}
