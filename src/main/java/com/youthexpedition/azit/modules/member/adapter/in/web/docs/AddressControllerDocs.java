package com.youthexpedition.azit.modules.member.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.RegisterAddressRequest;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.UpdateAddressRequest;
import com.youthexpedition.azit.modules.member.application.port.in.dto.AddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Address" , description = "주소 API")
public interface AddressControllerDocs {

    @Operation(
            summary = "배송지 등록",
            description = """
            신규 배송지 정보를 등록합니다. <br><br>
            
            **[참고 사항]** <br>
            * 수령인, 연락처, 우편번호, 기본 주소, 상세 주소는 모두 필수 입력 항목입니다. (INVALID_ADDRESS_INPUT)
            * 사용자의 첫 번째 배송지 등록인 경우, 요청값과 관계없이 자동으로 '기본 배송지'로 설정됩니다.
            * 새 주소를 기본 배송지(isDefault: true)로 등록할 경우, 기존에 설정된 기본 배송지는 자동으로 일반 배송지로 변경됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "INVALID_ADDRESS_INPUT", "MEMBER_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> registerAddress(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody RegisterAddressRequest request);

    @Operation(
            summary = "배송지 수정",
            description = """
            기존 배송지 정보를 수정합니다. <br><br>
            
            **[참고 사항]** <br>
            * 본인의 배송지만 수정 가능합니다. (FORBIDDEN_ADDRESS_ACCESS)
            * 모든 필드는 필수 입력 항목입니다. (INVALID_ADDRESS_INPUT)
            * 해당 주소를 '기본 배송지'로 변경 시, 기존의 다른 기본 배송지는 해제됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "ADDRESS_NOT_FOUND", "FORBIDDEN_ADDRESS_ACCESS", "INVALID_ADDRESS_INPUT",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> updateAddress(
            @Parameter(hidden = true) @CurrentMemberId Long memberId, @PathVariable Long addressId, @Valid @RequestBody UpdateAddressRequest request);

    @Operation(
            summary = "배송지 삭제",
            description = """
            등록된 배송지를 삭제합니다. <br><br>
            
            **[참고 사항]** <br>
            * 본인의 배송지만 삭제 가능합니다. (FORBIDDEN_ADDRESS_ACCESS)
            * MVP 정책: 기본 배송지 여부와 상관없이 삭제가 가능합니다.
            """
    )
    @ApiErrorCodeExamples({
            "ADDRESS_NOT_FOUND", "FORBIDDEN_ADDRESS_ACCESS",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> deleteAddress(@Parameter(hidden = true) @CurrentMemberId Long memberId, @PathVariable Long addressId);

    @Operation(
            summary = "배송지 목록 조회",
            description = """
            사용자의 모든 배송지 목록을 조회합니다. <br><br>
            
            **[정렬 기준]** <br>
            1. 기본 배송지가 가장 상단에 노출됩니다. <br>
            2. 그 외 주소는 최신 등록순으로 정렬됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<AddressResponse>> getAddresses(@Parameter(hidden = true) @CurrentMemberId Long memberId);
}
