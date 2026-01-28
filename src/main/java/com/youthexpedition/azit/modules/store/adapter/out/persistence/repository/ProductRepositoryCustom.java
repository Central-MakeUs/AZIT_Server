package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;

import java.util.Optional;

public interface ProductRepositoryCustom {
    SliceResponse<ProductListResponse> findProducts(GetProductListQuery query);
    Optional<ProductEntity> findByIdWithAllDetails(Long productId);
    Optional<ProductEntity> findByIdForCart(Long productId);
}
