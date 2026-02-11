package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageResponse(
        @Schema(description = "사용자 ID")
        Long id,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "크루 내 역할")
        CrewMemberRole crewMemberRole,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "누적 출석 횟수")
        Integer totalAttendanceCount,
        @Schema(description = "포인트")
        Long totalPoints
) {
    public static MyPageResponse of(Long id, String nickname, CrewMemberRole crewMemberRole, String profileImageUrl, Integer totalAttendanceCount, Long totalPoints) {
        return new MyPageResponse(id, nickname, crewMemberRole, profileImageUrl, totalAttendanceCount, totalPoints);
    }
}