package com.youthexpedition.azit.infrastructure.common.query;

public record CursorPageQuery(
        Long cursorId, // 마지막으로 조회된 아이템 ID
        int size       // 한 번에 가져올 아이템 개수
) {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static CursorPageQuery of(Long cursorId, Integer size) {
        int pageSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;

        if (pageSize > MAX_SIZE) {
            pageSize = MAX_SIZE;
        }

        return new CursorPageQuery(cursorId, pageSize);
    }
}
