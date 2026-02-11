package com.youthexpedition.azit.modules.member.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AgreeToTermsRequest;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member" , description = "회원 API")
public interface MemberControllerDocs {

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
    CommonResponse<Void> agreeToTerms(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody AgreeToTermsRequest request);

    @Operation(
            summary = "회원 탈퇴",
            description = """
            서비스 이용을 중단하고 회원의 소셜 연동 해제 및 탈퇴 처리를 진행합니다. <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_ALREADY_WITHDRAWN", "APPLE_REVOKE_FAILED",  "KAKAO_REVOKE_FAILED",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> withdraw(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Parameter(hidden = true) @CurrentAccessToken String accessToken);

    @Operation(
            summary = "가입 승인/거절 결과 확인",
            description = """
            사용자가 가입 요청에 대한 승인 또는 거절 결과를 확인했음을 서버에 알립니다. <br><br>
            
            **[참고 사항]** <br>
            * APPROVED_PENDING_CONFIRM 또는 REJECTED_PENDING_CONFIRM 상태인 회원만 호출 가능합니다. (INVALID_MEMBER_STATUS)
            * 가입 승인 완료 화면에서 '홈으로' 버튼 클릭 시 호출: 멤버 상태가 APPROVED_PENDING_CONFIRM -> ACTIVE로 변경됩니다.
            * 가입 거절 안내 화면에서 '처음으로' 버튼 클릭 시 호출: 멤버 상태가 REJECTED_PENDING_CONFIRM -> PENDING_ONBOARDING으로 변경됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND", "INVALID_MEMBER_STATUS",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> confirmMemberStatus(@Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "내 정보 조회(마이페이지)",
            description = """
            로그인한 사용자의 기본 정보를 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 소속 크루가 없으면 null을 반환합니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<MyPageResponse> getMyInfo(@Parameter(hidden = true) @CurrentMemberId Long memberId
    );


}
