package com.youthexpedition.azit.modules.member.domain.model.enums;

public enum MemberStatus {
    ACTIVE, // 리더로서 크루를 생성 완료했거나, 크루원으로서 승인이 완료된 상태 (정회원)
    WITHDRAWN, // 탈퇴
    PENDING_TERMS, // 약관 동의 전
    PENDING_ONBOARDING, // 약관은 동의했으나, 리더/크루원 선택 전
    WAITING_FOR_APPROVE, // 크루원으로서 코드를 입력하고 가입 승인을 기다리는 상태
    APPROVED_PENDING_CONFIRM, // 가입 승인 - 확인 전
    REJECTED_PENDING_CONFIRM; // 가입 거절 - 확인 전

    public boolean isCrewInfoRequired() {
        return this == ACTIVE ||
                this == WAITING_FOR_APPROVE ||
                this == APPROVED_PENDING_CONFIRM ||
                this == REJECTED_PENDING_CONFIRM;
    }
}
