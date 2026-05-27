package com.youthexpedition.azit.modules.test.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.test.adapter.in.web.docs.TestMemberControllerDocs;
import com.youthexpedition.azit.modules.test.application.port.in.TestMemberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/members")
@RequiredArgsConstructor
public class TestMemberController implements TestMemberControllerDocs {
    private final TestMemberUseCase testMemberUseCase;

    @PostMapping("/me/withdraw-immediately")
    public CommonResponse<Void> forceWithdraw(@CurrentMemberId Long memberId, @CurrentAccessToken String accessToken) {
        testMemberUseCase.forceWithdraw(memberId, accessToken);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
