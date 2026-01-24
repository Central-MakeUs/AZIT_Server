package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;

public interface ProductRepositoryCustom {
    SliceResponse<ProductListResponse> findProducts(GetProductListQuery query);
}
