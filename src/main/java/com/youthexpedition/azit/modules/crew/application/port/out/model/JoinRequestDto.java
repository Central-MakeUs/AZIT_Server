package com.youthexpedition.azit.modules.crew.application.port.out.model;

import java.time.LocalDateTime;

public record JoinRequestDto(
        Long memberId,
        String nickname,
        String profileImageUrl,
        LocalDateTime requestedAt
) {
}
