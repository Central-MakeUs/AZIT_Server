package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.application.port.in.ProductUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService implements ProductUseCase {
    private final LoadProductPort loadProductPort;

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ProductListResponse> getProducts(GetProductListQuery query) {
        return loadProductPort.findProducts(query);
    }
}
