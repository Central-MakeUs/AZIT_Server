package com.youthexpedition.azit.modules.member.adapter.in.web;

import com.youthexpedition.azit.infrastructure.auth.util.TokenUtil;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.member.adapter.in.web.docs.MemberControllerDocs;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AgreeToTermsRequest;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {
    private final MemberUseCase memberUseCase;

    @PostMapping("/terms")
    public CommonResponse<Void> agreeToTerms(@CurrentMemberId Long memberId, @Valid @RequestBody AgreeToTermsRequest request) {
        memberUseCase.agreeToTerms(memberId, request.toCommand());
        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/me/withdraw")
    public CommonResponse<Void> withdraw(
            @CurrentMemberId Long memberId, @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = TokenUtil.extractToken(authorizationHeader);
        memberUseCase.withdraw(memberId, accessToken);
        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
