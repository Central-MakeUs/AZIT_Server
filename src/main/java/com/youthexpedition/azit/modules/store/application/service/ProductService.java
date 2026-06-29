package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.application.port.in.ProductUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import com.youthexpedition.azit.modules.store.application.service.mapper.ProductResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements ProductUseCase {
    private final LoadProductPort loadProductPort;
    private final ProductResponseMapper productResponseMapper;

    @Override
    public SliceResponse<ProductListResponse> getProducts(CursorPageQuery query) {
        SliceResponse<Product> productSlice = loadProductPort.findProducts(query);

        List<ProductListResponse> responses = productSlice.content().stream()
                .map(productResponseMapper::toListResponse)
                .toList();

        return new SliceResponse<>(responses, productSlice.hasNext(), productSlice.lastId());
    }

    @Override
    public ProductDetailResponse getProduct(Long productId) {
        Product product = loadProductPort.findById(productId)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.PRODUCT_NOT_FOUND));

        return productResponseMapper.toDetailResponse(product);
    }
}
