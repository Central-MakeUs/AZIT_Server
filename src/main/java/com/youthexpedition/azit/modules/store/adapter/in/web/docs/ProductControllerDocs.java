package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

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
    CommonResponse<SliceResponse<ProductListResponse>> getProducts(CursorPageQuery query);

    @Operation(
            summary = "상품 상세 정보 조회",
            description = """
            상품의 상세 정보, 이미지 리스트, 옵션 그룹 및 재고 정보를 조회합니다. <br><br>
            
            **[데이터 구조]** <br>
            * expectedShippingDate: 상품의 배송 출고 소요 시간을 기준으로 자동 계산된 예상 배송 시작일입니다.
            * slideImageUrls: 상품 상단 배너에 노출할 이미지 리스트입니다.
            * detailImageUrls: 상품 하단 상세 설명 영역에 노출할 이미지 리스트입니다.
            * skus: 선택 가능한 옵션 조합(SKU) 리스트입니다. 각 SKU는 optionValueIds 리스트를 통해 어떤 옵션값들의 조합인지 나타냅니다.
            
            **[참고 사항]** <br>
            * 옵션 관련 데이터들은 DB에 저장된 노출 순서를 기준으로 정렬되어 있습니다.
            """
    )
    @ApiErrorCodeExamples({
            "PRODUCT_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<ProductDetailResponse> getProduct(@Parameter(description = "조회할 상품 ID") @PathVariable Long productId);
}
