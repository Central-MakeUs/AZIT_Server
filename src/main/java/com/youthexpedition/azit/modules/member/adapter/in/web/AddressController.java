package com.youthexpedition.azit.modules.member.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.member.adapter.in.web.docs.AddressControllerDocs;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AddressRegisterRequest;
import com.youthexpedition.azit.modules.member.application.port.in.AddressUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressControllerDocs {
    private final AddressUseCase addressUseCase;

    @PostMapping
    public CommonResponse<Void> registerAddress(@CurrentMemberId Long memberId, @RequestBody @Valid AddressRegisterRequest request) {
        addressUseCase.registerAddress(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
