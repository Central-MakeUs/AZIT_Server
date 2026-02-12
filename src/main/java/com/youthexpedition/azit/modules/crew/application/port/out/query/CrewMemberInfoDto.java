package com.youthexpedition.azit.modules.crew.application.port.out.query;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;

import java.time.LocalDateTime;

public record CrewMemberInfoDto(
        Long id,
        Long memberId,
        String nickname,
        String profileImageUrl,
        CrewMemberRole role,
        LocalDateTime joinedAt
) {
}
