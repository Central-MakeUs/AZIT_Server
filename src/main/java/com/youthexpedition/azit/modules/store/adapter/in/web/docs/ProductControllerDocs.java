package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Product" , description = "상품 API")
public interface ProductControllerDocs {

    @Operation(
            summary = "상품 목록 조회 (무한 스크롤)",
            description = """
            커서 기반 페이징을 사용하여 전체 상품 목록을 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 최신순 정렬: 가장 최근에 등록된 상품부터 정렬되어 반환됩니다.
            * 무한 스크롤 방식: hasNext를 통해 다음 페이지 존재 여부를 확인하고, lastId를 다음 요청의 cursorId로 호출하면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<SliceResponse<ProductListResponse>> getProducts(
            @Parameter(description = "마지막으로 조회된 상품 ID (첫 페이지 조회 시 null 또는 넣지 않아도 됨)")
            @RequestParam(required = false) Long cursorId,
            @Parameter(description = "한 번에 조회할 상품 개수")
            @RequestParam(defaultValue = "20") int size);
}
