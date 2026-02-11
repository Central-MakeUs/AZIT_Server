package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record CrewMemberListResponse(
        @Schema(description = "총 멤버 수")
        long totalCount,
        @Schema(description = "멤버 목록")
        List<CrewMemberDetailResponse> memberList
) {
    public static CrewMemberListResponse of(List<CrewMemberDetailResponse> members) {
        return new CrewMemberListResponse(members.size(), members);
    }

    public record CrewMemberDetailResponse(
            @Schema(description = "멤버 ID")
            Long id,
            @Schema(description = "닉네임")
            String nickname,
            @Schema(description = "프로필 이미지 URL")
            String profileImageUrl,
            @Schema(description = "크루 내 역할")
            CrewMemberRole role,
            @Schema(description = "가입일")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime joinedDate
    ) {}
}
