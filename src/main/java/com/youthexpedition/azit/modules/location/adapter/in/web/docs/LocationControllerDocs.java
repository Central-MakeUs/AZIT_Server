package com.youthexpedition.azit.modules.location.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.location.application.port.in.dto.LocationSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Location" , description = "장소 API")
public interface LocationControllerDocs {

    @Operation(
            summary = "장소 검색",
            description = """
            네이버 지역 검색 API를 호출하여 입력한 키워드에 부합하는 장소 목록을 최대 5개 반환합니다. <br>
            일정 등록 시 집합 장소를 검색하기 위한 용도로 사용됩니다. <br><br>
            
            **[참고 사항]** <br>
            * 도로명 주소를 우선적으로 반환하며, 도로명 주소가 없을 경우 지번 주소를 반환합니다. <br>
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<LocationSearchResponse>> search(@RequestParam String query);

}
