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
            카카오 등 소셜 플랫폼의 인가 코드를 통해 로그인을 진행하고 JWT 토큰과 회원의 현재 상태, 최근 가입한 크루 ID를 반환합니다. <br><br>
            
            **[참고 사항]** <br>
            * 보안을 위해 리프레시 토큰은 HttpOnly 쿠키에 저장되어 발급됩니다.
            * 응답으로 받은 **status** 값에 따라 앱의 초기 진입 화면(약관 동의, 온보딩, 메인 등)을 결정해야 합니다. <br>
            """
    )
    @ApiErrorCodeExamples({
            "INVALID_SOCIAL_CODE", "SOCIAL_AUTHENTICATION_FAILED"
    })
    CommonResponse<List<LocationSearchResponse>> search(@RequestParam String query);

}
