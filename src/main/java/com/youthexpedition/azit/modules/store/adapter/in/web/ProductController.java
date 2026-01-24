package com.youthexpedition.azit.modules.store.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.store.adapter.in.web.docs.ProductControllerDocs;
import com.youthexpedition.azit.modules.store.adapter.in.web.mapper.ProductWebMapper;
import com.youthexpedition.azit.modules.store.application.port.in.ProductUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {
    private final ProductUseCase productUseCase;
    private final ProductWebMapper productWebMapper;

    @GetMapping
    public CommonResponse<SliceResponse<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "20") int size) {
        GetProductListQuery query = productWebMapper.toQuery(cursorId, size);
        SliceResponse<ProductListResponse> result = productUseCase.getProducts(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, result);
    }
}
