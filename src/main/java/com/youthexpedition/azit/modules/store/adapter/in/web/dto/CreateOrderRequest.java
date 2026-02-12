package com.youthexpedition.azit.modules.store.adapter.in.web.dto;

import com.youthexpedition.azit.modules.store.application.port.in.command.CreateOrderCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Objects;

public record CreateOrderRequest(
        @Schema(description = "주문할 장바구니 아이템 ID 리스트(장바구니 구매용)")
        List<Long> cartItemIds,

        @Schema(description = "sku ID(바로 구매용)")
        Long skuId,

        @Schema(description = "구매할 상품 수량(바로 구매용)")
        Integer quantity,

        @Schema(description = "수령인 이름")
        @NotBlank(message = "수령인 이름은 필수입니다.")
        String recipientName,

        @Schema(description = "수령인 연락처")
        @NotBlank(message = "수령인 연락처는 필수입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

        @Schema(description = "기본 주소")
        @NotBlank(message = "기본 주소는 필수입니다.")
        String baseAddress,

        @Schema(description = "상세 주소")
        @NotBlank(message = "상세 주소는 필수입니다.")
        String detailAddress,

        @Schema(description = "배송 요청사항")
        @Size(max = 100, message = "배송 요청사항은 100자 이내로 입력해주세요.")
        String shippingInstruction,

        @Schema(description = "사용할 포인트")
        @Min(0)
        long usedPoints,

        @Schema(description = "결제 수단")
        @NotBlank(message = "결제 수단을 선택해주세요.")
        String paymentMethod,

        @Schema(description = "입금자명(무통장입금)")
        @Size(max = 50, message = "입금자명은 최대 50자까지 입력 가능합니다.")
        String depositorName

) {
    public CreateOrderCommand toCommand(Long memberId) {
        return CreateOrderCommand.of(
                memberId,
                cartItemIds != null
                        ? cartItemIds.stream().filter(Objects::nonNull).toList()
                        : List.of(), // null이면 빈 리스트로 대체
                skuId,
                quantity,
                recipientName,
                phoneNumber,
                baseAddress,
                detailAddress,
                shippingInstruction,
                usedPoints,
                paymentMethod,
                depositorName != null ? depositorName.trim() : null
        );
    }
}
