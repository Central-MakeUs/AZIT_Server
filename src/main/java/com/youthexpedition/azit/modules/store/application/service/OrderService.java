package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.DeliveryAddressResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.store.application.port.in.OrderUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.out.LoadCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.service.mapper.OrderResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.PointPolicy;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements OrderUseCase {
    private final LoadMemberPort loadMemberPort;
    private final LoadDeliveryAddressPort loadDeliveryAddressPort;
    private final LoadCartPort loadCartPort;
    private final OrderResponseMapper orderResponseMapper;
    private final DeliveryAddressResponseMapper deliveryAddressResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderCheckoutResponse getCheckoutInfo(Long memberId, List<Long> cartItemIds) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 기본 배송지 조회 (없으면 null)
        DeliveryAddressResponse defaultAddress = loadDeliveryAddressPort.findDefaultByMemberId(memberId)
                .map(deliveryAddressResponseMapper::toAddressResponse)
                .orElse(null);

        // 주문할 장바구니 아이템 상세 조회
        List<CartItemQueryDto> items = loadCartPort.findCartDetailsByIds(cartItemIds);

        // 결제 수단 설정
        List<OrderCheckoutResponse.PaymentMethodResponse> paymentMethods = List.of(
                OrderCheckoutResponse.PaymentMethodResponse.of(PaymentMethod.NAVER_PAY.getCode(), PaymentMethod.NAVER_PAY.getLabel(), false),
                OrderCheckoutResponse.PaymentMethodResponse.of(PaymentMethod.BANK_TRANSFER.getCode(), PaymentMethod.BANK_TRANSFER.getLabel(), true)
        );

        // 배송비 계산
        // 브랜드별 최대 배송비 저장하는 맵
        Map<Long, Long> brandMaxShippingFeesMap = new HashMap<>();

        for (CartItemQueryDto cartItem : items) {
            // 브랜드별로 가장 높은 배송비를 선택하여 저장
            brandMaxShippingFeesMap.merge(cartItem.brandId(), cartItem.shippingFee(), Long::max);
        }

        long totalShippingFee = brandMaxShippingFeesMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return orderResponseMapper.toOrderCheckoutResponse(member, defaultAddress, items, paymentMethods, totalShippingFee);
    }

    // 포인트 사용 유효성 검증
    public void validatePointUsage(Long memberId, long usePoints) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        PointPolicy.validate(member, usePoints);
    }

}
