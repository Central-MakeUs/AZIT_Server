package com.youthexpedition.azit.modules.member.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AgreeToTermsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member" , description = "회원 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "약관 동의",
            description = """
            소셜 로그인 직후 'PENDING_TERMS' 상태인 회원이 필수 약관에 동의하는 단계입니다. <br>
            성공 시 회원의 상태는 'PENDING_ONBOARDING'으로 변경되며, 이후 온보딩(크루 참여/생성)이 가능해집니다. <br>
            PENDING_TERMS 상태의 회원만 약관 동의 API를 호출할 수 있으므로 다른 상태일 경우 INVALID_MEMBER_STATUS 오류가 발생합니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND",
            "REQUIRED_TERMS_NOT_AGREED", // 필수 약관 중 하나라도 동의하지 않은 경우
            "INVALID_MEMBER_STATUS"      // 이미 약관 동의를 완료했거나 탈퇴한 유저인 경우
    })
    CommonResponse<Void> agreeToTerms(@CurrentMemberId Long memberId, @Valid @RequestBody AgreeToTermsRequest request);

}
