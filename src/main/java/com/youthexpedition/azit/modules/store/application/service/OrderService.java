package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.DeliveryAddressResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.store.application.port.in.OrderUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.command.CreateOrderCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.out.LoadCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveOrderPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.service.mapper.OrderResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.*;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements OrderUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final LoadDeliveryAddressPort loadDeliveryAddressPort;
    private final SaveProductPort saveProductPort;
    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final SaveOrderPort saveOrderPort;
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
        List<CartItemQueryDto> cartItems = loadCartPort.findCartDetailsByIds(cartItemIds);

        long totalProductPrice = OrderPricePolicy.calculateTotalProductPrice(cartItems);
        long membershipDiscount = OrderPricePolicy.calculateTotalMembershipDiscount(cartItems);
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(cartItems);

        return orderResponseMapper.toOrderCheckoutResponse(member, defaultAddress, cartItems, totalProductPrice, membershipDiscount, totalShippingFee);
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        // 포인트 사용 유효성 검증 및 회원 조회
        Member member = validatePointUsage(command.memberId(), command.usedPoints());

        // 장바구니 상세 정보 조회
        List<CartItemQueryDto> cartItems = loadCartPort.findCartDetailsByIds(command.cartItemIds());
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(cartItems);

        // 결제 수단 확인 및 처리 (MVP에서는 무통장 입금만 지원)
        PaymentMethod paymentMethod = PaymentMethod.valueOf(command.paymentMethod());
        handlePayment(paymentMethod);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> {
                    // 재고 차감
                    saveProductPort.decreaseStock(item.skuId(), item.quantity());

                    return OrderItem.create(
                            item.productId(),
                            item.skuId(),
                            item.productName(),
                            orderResponseMapper.formatOptionValues(item.optionValues()), // 옵션 포맷팅 재활용
                            item.basePrice() + item.additionalPrice(),
                            item.salePrice() + item.additionalPrice(),
                            item.quantity()
                    );
                }).toList();

        Order order = Order.create(
                command.memberId(),
                Order.generateOrderNumber(),
                OrderAddress.builder()
                        .recipientName(command.recipientName())
                        .phoneNumber(command.phoneNumber())
                        .baseAddress(command.baseAddress())
                        .detailAddress(command.detailAddress())
                        .build(),
                command.shippingInstruction(),
                totalShippingFee,
                command.usedPoints(),
                PaymentMethod.valueOf(command.paymentMethod()),
                orderItems
        );

        Order savedOrder = saveOrderPort.save(order);

        // 포인트 차감
        member.deductPoints(command.usedPoints());
        saveMemberPort.save(member);

        // 장바구니 비우기
        saveCartPort.deleteAllByMemberIdAndIds(command.memberId(), command.cartItemIds());

        return orderResponseMapper.toCreateOrderResponse(savedOrder);
    }

    // 포인트 사용 유효성 검증
    public Member validatePointUsage(Long memberId, long usePoints) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        PointPolicy.validate(member, usePoints);

        return member;
    }

    // 결제 수단별 처리
    private void handlePayment(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
            // 무통장 입금은 별도 API 연동 없이 주문 승인 대기 상태로 진행
            return;
        }

        // 지원하지 않는 결제수단 체크
        if (!paymentMethod.isEnabled()) throw new BusinessException(StoreErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
    }

}
