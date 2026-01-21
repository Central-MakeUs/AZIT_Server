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
        this.status = CrewMemberStatus.JOINED;
    }

    // 가입 거절
    public void reject() {
        this.status = CrewMemberStatus.REJECTED;
    }
}
