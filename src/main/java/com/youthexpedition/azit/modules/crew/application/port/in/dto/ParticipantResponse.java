package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ParticipantResponse(
        @Schema(description = "멤버 ID")
        Long memberId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "크루 내 역할")
        CrewMemberRole role,
        @Schema(description = "일정 생성자 여부")
        boolean isCreator,
        @Schema(description = "신청 시간")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime participatedAt
) {
    public static ParticipantResponse of(
            Long memberId,
            String nickname,
            String profileImageUrl,
            CrewMemberRole role,
            boolean isCreator,
            LocalDateTime participatedAt
    ) {
        return new ParticipantResponse(memberId, nickname, profileImageUrl, role, isCreator, participatedAt);
    }
}
