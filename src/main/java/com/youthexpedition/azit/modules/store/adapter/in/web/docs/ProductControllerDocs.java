package com.youthexpedition.azit.modules.store.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Product" , description = "회원 API")
public interface ProductControllerDocs {

    @Operation(
            summary = "약관 동의",
            description = """
            소셜 로그인 직후 '약관 동의 대기(PENDING_TERMS)' 상태인 회원이 필수 서비스 약관에 동의하는 단계입니다. <br><br>
            
            **[제약 사항]** <br>
            * '약관 동의 대기(PENDING_TERMS)' 상태의 회원만 호출 가능합니다. (INVALID_MEMBER_STATUS)
            * 필수 약관 중 하나라도 누락될 경우 가입이 진행되지 않습니다. (REQUIRED_TERMS_NOT_AGREED)
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND",
            "REQUIRED_TERMS_NOT_AGREED", // 필수 약관 중 하나라도 동의하지 않은 경우
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    public CommonResponse<SliceResponse<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "20") int size);
}
