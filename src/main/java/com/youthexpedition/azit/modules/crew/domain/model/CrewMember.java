package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CrewMember {
    private final Long id;
    private final Long crewId;
    private final Long memberId;
    private CrewMemberRole role;
    private CrewMemberStatus status;
    private LocalDateTime expelledAt;
    private LocalDateTime exitedAt;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static final long REJOINING_COOLDOWN_HOURS = 24;

    // 리더 등록
    public static CrewMember createAsLeader(Long crewId, Long memberId) {
        return CrewMember.builder()
                .crewId(crewId)
                .memberId(memberId)
                .role(CrewMemberRole.LEADER)
                .status(CrewMemberStatus.JOINED)
                .build();
    }

    // 멤버 등록
    public static CrewMember createAsMember(Long crewId, Long memberId) {
        return CrewMember.builder()
                .crewId(crewId)
                .memberId(memberId)
                .role(CrewMemberRole.MEMBER)
                .status(CrewMemberStatus.REQUESTED)
                .build();
    }

    // 가입 신청 상태인지 확인
    public boolean isJoinRequested() {
        return this.status == CrewMemberStatus.REQUESTED;
    }

    // 가입 승인
    public void approve() {
        this.status = CrewMemberStatus.JOINED;
    }

    // 가입 거절
    public void reject() {
        this.status = CrewMemberStatus.REJECTED;
    }

    // 크루 탈퇴
    public void exit(LocalDateTime exitedAt) {
        this.status = CrewMemberStatus.EXITED;
        this.exitedAt = exitedAt;
    }

    // 자진 탈퇴 후 재가입 쿨다운 여부 확인 (24시간)
    public boolean isExitCooldownActive(LocalDateTime now) {
        if (this.exitedAt == null) return false;
        return this.exitedAt.plusHours(REJOINING_COOLDOWN_HOURS).isAfter(now);
    }

    // 크루 방출
    public void expel(LocalDateTime expelledAt) {
        this.status = CrewMemberStatus.EXPELLED;
        this.expelledAt = expelledAt;
    }

    // 방출 후 재가입 쿨다운 여부 확인 (24시간)
    public boolean isRejoinCooldownActive(LocalDateTime now) {
        if (this.expelledAt == null) return false;
        return this.expelledAt.plusHours(REJOINING_COOLDOWN_HOURS).isAfter(now);
    }

    // 가입 신청 취소 (본인)
    public void cancel(LocalDateTime cancelledAt) {
        this.status = CrewMemberStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    // 취소 후 재신청 쿨다운 여부 확인 (24시간)
    public boolean isCancelCooldownActive(LocalDateTime now) {
        if (this.cancelledAt == null) return false;
        return this.cancelledAt.plusHours(REJOINING_COOLDOWN_HOURS).isAfter(now);
    }

    // 가입 재신청
    public void reJoin() {
        this.status = CrewMemberStatus.REQUESTED;
    }
}
