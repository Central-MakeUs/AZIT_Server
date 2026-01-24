package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;

public interface ProductUseCase {
    SliceResponse<ProductListResponse> getProducts(GetProductListQuery query);
}
