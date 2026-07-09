package com.youthexpedition.azit.modules.member.domain.model.enums;

public enum MemberStatus {
    ACTIVE,          // 약관 동의 완료, 앱 사용 가능
    WITHDRAWN,       // 탈퇴 (유예기간 중, 재로그인 시 복구 가능)
    DELETED,         // 유예기간 만료 후 개인정보 파기 완료 (복구 불가)
    PENDING_TERMS;   // 약관 동의 전

    // 크루 가입/생성 가능한 상태인지 확인
    public boolean isJoinable() {
        return this == ACTIVE;
    }
}
