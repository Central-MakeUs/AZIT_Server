package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CrewMemberListResponse(
        @Schema(description = "전체 멤버 수")
        long totalCount,

        @Schema(description = "멤버 목록")
        List<CrewMemberDetailResponse> content,

        @Schema(description = "남은 페이지가 있는지 여부 (false면 마지막 페이지)")
        boolean hasNext,

        @Schema(description = "마지막 데이터의 ID, 다음 페이지 호출 시 해당 id를 cursorId에 넣어서 호출")
        Long lastId
) {
    public static CrewMemberListResponse of(long totalCount, List<CrewMemberDetailResponse> content, boolean hasNext, Long lastId) {
        return new CrewMemberListResponse(totalCount, content, hasNext, lastId);
    }
}
