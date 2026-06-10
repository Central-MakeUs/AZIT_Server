package com.youthexpedition.azit.modules.auth.domain.model;

import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import lombok.Builder;

@Builder
public record AuthResult(
        AuthToken authToken,
        MemberStatus status,
        Long crewId,
        boolean needsTermsUpdate
) {
}
