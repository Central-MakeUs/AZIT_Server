package com.youthexpedition.azit.modules.member.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.member.adapter.in.web.docs.AddressControllerDocs;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.RegisterAddressRequest;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.UpdateAddressRequest;
import com.youthexpedition.azit.modules.member.application.port.in.AddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.dto.AddressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressControllerDocs {
    private final AddressUseCase addressUseCase;

    @PostMapping
    public CommonResponse<Void> registerAddress(@CurrentMemberId Long memberId, @RequestBody @Valid RegisterAddressRequest request) {
        addressUseCase.registerAddress(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PutMapping("/{addressId}")
    public CommonResponse<Void> updateAddress(@CurrentMemberId Long memberId, @PathVariable Long addressId, @RequestBody @Valid UpdateAddressRequest request) {
        addressUseCase.updateAddress(request.toCommand(memberId, addressId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{addressId}")
    public CommonResponse<Void> deleteAddress(@CurrentMemberId Long memberId, @PathVariable Long addressId) {
        addressUseCase.deleteAddress(memberId, addressId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping
    public CommonResponse<List<AddressResponse>> getAddresses(@CurrentMemberId Long memberId) {
        List<AddressResponse> responses = addressUseCase.getAddresses(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, responses);
    }
}
