package com.youthexpedition.azit.modules.store.application.port.in.query;

public record GetProductListQuery(
        Long cursorId, // 마지막으로 조회된 상품 ID
        int size       // 한 번에 가져올 상품 개수
) {
}
