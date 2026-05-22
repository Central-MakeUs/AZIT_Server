package com.youthexpedition.azit.modules.member.domain.model.enums;

public enum MemberStatus {
    ACTIVE,          // 약관 동의 완료, 앱 사용 가능
    WITHDRAWN,       // 탈퇴
    PENDING_TERMS;   // 약관 동의 전

    // 크루 가입/생성 가능한 상태인지 확인
    public boolean isJoinable() {
        return this == ACTIVE;
    }
}
