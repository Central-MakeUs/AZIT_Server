package com.youthexpedition.azit.infrastructure.common.query;

public record CursorPageQuery(
        Long cursorId, // 마지막으로 조회된 ID
        int size       // 한 번에 가져올 상품 개수
) {
}
