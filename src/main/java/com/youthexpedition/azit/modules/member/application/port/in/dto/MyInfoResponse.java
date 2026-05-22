package com.youthexpedition.azit.modules.member.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyInfoResponse(
        @Schema(description = "사용자 ID")
        Long id,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "누적 출석 횟수")
        Integer totalAttendanceCount,
        @Schema(description = "포인트")
        Long totalPoints
) {
    public static MyInfoResponse of(Long id, String nickname, String profileImageUrl,
                                    Integer totalAttendanceCount, Long totalPoints) {
        return new MyInfoResponse(id, nickname, profileImageUrl, totalAttendanceCount, totalPoints);
    }
}
