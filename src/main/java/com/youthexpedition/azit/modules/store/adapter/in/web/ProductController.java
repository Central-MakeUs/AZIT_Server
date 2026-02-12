package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.ProductControllerDocs;
import com.youthexpedition.azit.modules.store.application.port.in.ProductUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {
    private final ProductUseCase productUseCase;

    @GetMapping
    public CommonResponse<SliceResponse<ProductListResponse>> getProducts(CursorPageQuery query) {
        SliceResponse<ProductListResponse> result = productUseCase.getProducts(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }

    @GetMapping("/{productId}")
    public CommonResponse<ProductDetailResponse> getProduct(@PathVariable Long productId) {
        ProductDetailResponse result = productUseCase.getProduct(productId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }
}
