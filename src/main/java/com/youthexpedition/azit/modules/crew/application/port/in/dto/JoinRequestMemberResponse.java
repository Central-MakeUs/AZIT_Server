package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import java.time.LocalDateTime;

public record JoinRequestMemberResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        LocalDateTime requestedAt // 신청 시간 (필요시)
) {}
