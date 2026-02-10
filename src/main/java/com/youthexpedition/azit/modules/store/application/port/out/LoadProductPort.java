package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.Product;

import java.util.Optional;

public interface LoadProductPort {
    SliceResponse<ProductListResponse> findProducts(GetProductListQuery query);
    Optional<Product> findById(Long productId);
    Optional<Product> findByIdForCart(Long productId);
    Optional<CheckoutItemDto> findProductInfoBySkuId(Long skuId, int quantity); // 상품 바로 구매
}
