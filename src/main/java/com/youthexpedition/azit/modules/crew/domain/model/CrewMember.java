package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
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
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    // 가입 승인
    public void approve() {
        if (this.status != CrewMemberStatus.REQUESTED) {
            throw new BusinessException(CrewErrorCode.ALREADY_PROCESSED_JOIN_REQUEST);
        }
        this.status = CrewMemberStatus.JOINED;
    }

    // 가입 거절
    public void reject() {
        if (this.status != CrewMemberStatus.REQUESTED) {
            throw new BusinessException(CrewErrorCode.ALREADY_PROCESSED_JOIN_REQUEST);
        }
        this.status = CrewMemberStatus.REJECTED;
    }

    // 크루 탈퇴
    public void exit() {
        this.status = CrewMemberStatus.EXITED;
    }

    // 크루 방출
    public void expel() {
        this.status = CrewMemberStatus.EXPELLED;
    }

    // 가입 재신청
    public void reJoin() {
        this.status = CrewMemberStatus.REQUESTED;
    }
}
