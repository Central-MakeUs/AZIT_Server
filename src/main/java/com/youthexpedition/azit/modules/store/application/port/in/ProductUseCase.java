package com.youthexpedition.azit.modules.store.application.port.in;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;

public interface ProductUseCase {
    SliceResponse<ProductListResponse> getProducts(CursorPageQuery query);
    ProductDetailResponse getProduct(Long productId);
}
