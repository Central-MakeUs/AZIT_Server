package com.youthexpedition.azit.modules.test.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[TEST]", description = "테스트용 API (운영 환경에서 사용 금지)")
public interface TestMemberControllerDocs {

    @Operation(
            summary = "사용자 즉시 탈퇴",
            description = """
                    사용자 탈퇴 및 신규 가입 플로우 테스트를 위한 API입니다. <br>
                    실제 탈퇴와 달리 사용자 관련 데이터를 DB에서 즉시 삭제합니다. <br><br>

                    **[참고 사항]** <br>
                    * 주문 관련 데이터는 운영과 동일하게 삭제하지 않습니다. (스냅샷 저장 용도) <br><br>
                    """
    )
    CommonResponse<Void> forceWithdraw(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Parameter(hidden = true) @CurrentAccessToken String accessToken
    );
}
