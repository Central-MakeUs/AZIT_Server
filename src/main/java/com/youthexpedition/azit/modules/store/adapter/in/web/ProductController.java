package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.ProductControllerDocs;
import com.youthexpedition.azit.modules.store.adapter.in.web.mapper.ProductWebMapper;
import com.youthexpedition.azit.modules.store.application.port.in.ProductUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {
    private final ProductUseCase productUseCase;
    private final ProductWebMapper productWebMapper;

    @GetMapping
    public CommonResponse<SliceResponse<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "20") int size) {
        CursorPageQuery query = productWebMapper.toQuery(cursorId, size);
        SliceResponse<ProductListResponse> result = productUseCase.getProducts(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }

    @GetMapping("/{productId}")
    public CommonResponse<ProductDetailResponse> getProduct(@PathVariable Long productId) {
        ProductDetailResponse result = productUseCase.getProduct(productId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }
}
