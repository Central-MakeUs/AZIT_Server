package com.youthexpedition.azit.modules.crew.application.port.out.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record JoinRequestDto(
        @Schema(description = "멤버 ID")
        Long memberId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "가입 요청 날짜")
        LocalDateTime requestedAt
) {
}
