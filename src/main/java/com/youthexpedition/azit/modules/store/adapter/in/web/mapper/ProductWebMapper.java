package com.youthexpedition.azit.modules.store.adapter.in.web.mapper;

import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import org.springframework.stereotype.Component;

@Component
public class ProductWebMapper {
    public GetProductListQuery toQuery(Long cursorId, int size) {
        int pageSize = (size <= 0) ? 20 : size;

        return new GetProductListQuery(cursorId, pageSize);
    }
}
