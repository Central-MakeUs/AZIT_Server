package com.youthexpedition.azit.infrastructure.common.response;

import java.util.List;

/**
 * 무한 스크롤(Slice) 응답을 위한 공통 Record
 */
public record SliceResponse<T>(
        List<T> content,
        boolean hasNext, // false 면 마지막 페이지
        Long lastId // 다음 조회를 위한 커서 (마지막 아이템의 ID)
) {
}
