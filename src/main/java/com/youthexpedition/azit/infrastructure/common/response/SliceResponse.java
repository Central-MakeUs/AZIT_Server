package com.youthexpedition.azit.infrastructure.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 무한 스크롤(Slice) 응답을 위한 공통 Record
 */
public record SliceResponse<T>(
        @Schema(description = "데이터 내용")
        List<T> content,
        @Schema(description = "남은 페이지가 있는지 여부 (false면 마지막 페이지)")
        boolean hasNext,
        @Schema(description = "마지막 데이터의 ID, 다음 페이지 호출 시 해당 id를 cursorId에 넣어서 호출")
        Long lastId
) {
}
