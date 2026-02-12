package com.youthexpedition.azit.modules.member.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.member.adapter.in.web.docs.DeliveryAddressControllerDocs;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.RegisterDeliveryAddressRequest;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.UpdateDeliveryAddressRequest;
import com.youthexpedition.azit.modules.member.application.port.in.DeliveryAddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class DeliveryAddressController implements DeliveryAddressControllerDocs {
    private final DeliveryAddressUseCase deliveryAddressUseCase;

    @PostMapping
    public CommonResponse<Void> registerDeliveryAddress(@CurrentMemberId Long memberId, @RequestBody @Valid RegisterDeliveryAddressRequest request) {
        deliveryAddressUseCase.registerDeliveryAddress(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PutMapping("/{addressId}")
    public CommonResponse<Void> updateDeliveryAddress(@CurrentMemberId Long memberId, @PathVariable Long addressId, @RequestBody @Valid UpdateDeliveryAddressRequest request) {
        deliveryAddressUseCase.updateDeliveryAddress(request.toCommand(memberId, addressId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{addressId}")
    public CommonResponse<Void> deleteDeliveryAddress(@CurrentMemberId Long memberId, @PathVariable Long addressId) {
        deliveryAddressUseCase.deleteDeliveryAddress(memberId, addressId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping
    public CommonResponse<List<DeliveryAddressResponse>> getDeliveryAddresses(@CurrentMemberId Long memberId) {
        List<DeliveryAddressResponse> responses = deliveryAddressUseCase.getDeliveryAddresses(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, responses);
    }
}
