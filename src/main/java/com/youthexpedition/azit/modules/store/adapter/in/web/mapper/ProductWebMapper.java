package com.youthexpedition.azit.modules.store.adapter.in.web.mapper;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import org.springframework.stereotype.Component;

@Component
public class ProductWebMapper {
    public CursorPageQuery toQuery(Long cursorId, int size) {
        int pageSize = (size <= 0) ? 20 : size;

        return new CursorPageQuery(cursorId, pageSize);
    }
}
