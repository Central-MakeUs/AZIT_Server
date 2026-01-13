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
    private String joiningAnswer;
}
