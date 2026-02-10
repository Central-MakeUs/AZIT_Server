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
import com.youthexpedition.azit.modules.store.application.port.out.*;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.application.service.mapper.OrderResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.*;
import com.youthexpedition.azit.modules.store.domain.model.enums.OrderType;
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
    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderResponseMapper orderResponseMapper;
    private final DeliveryAddressResponseMapper deliveryAddressResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderCheckoutResponse getCheckoutInfoFromCart(Long memberId, List<Long> cartItemIds) {
        Member member = getMember(memberId);

        // 기본 배송지 조회
        DeliveryAddressResponse defaultAddress = getDefaultAddress(memberId);

        // 주문할 장바구니 아이템 상세 조회
        List<CheckoutItemDto> items = loadCartPort.findCartDetailsByIds(cartItemIds);

        return createCheckoutResponse(member, defaultAddress, items);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCheckoutResponse getCheckoutInfoDirect(Long memberId, Long skuId, Integer quantity) {
        Member member = getMember(memberId);

        // 기본 배송지 조회
        DeliveryAddressResponse defaultAddress = getDefaultAddress(memberId);

        // 주문할 상품 상세 조회
        CheckoutItemDto item = loadProductPort.findProductInfoBySkuId(skuId, quantity)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.SKU_NOT_FOUND));

        return createCheckoutResponse(member, defaultAddress, List.of(item));
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        // 포인트 사용 유효성 검증 및 회원 조회
        Member member = validatePointUsage(command.memberId(), command.usedPoints());

        // 결제 타입 케이스별로 상품 아이템 조회
        List<CheckoutItemDto> items = switch (OrderType.from(command)) {
            case CART -> loadCartPort.findCartDetailsByIds(command.cartItemIds());

            case DIRECT -> List.of(loadProductPort.findProductInfoBySkuId(command.skuId(), command.quantity())
                    .orElseThrow(() -> new BusinessException(StoreErrorCode.SKU_NOT_FOUND)));

            case INVALID -> throw new BusinessException(StoreErrorCode.INVALID_ORDER_REQUEST);
        };

        // 배송비 계산
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(items);

        // 결제 수단 확인 및 처리 (MVP에서는 무통장 입금만 지원)
        handlePayment(PaymentMethod.valueOf(command.paymentMethod()));

        List<OrderItem> orderItems = items.stream()
                .map(productInfo -> {
                    // 재고 차감
                    saveProductPort.decreaseStock(productInfo.skuId(), productInfo.quantity());

                    return OrderItem.create(
                            productInfo.productId(),
                            productInfo.skuId(),
                            productInfo.productName(),
                            orderResponseMapper.formatOptionValues(productInfo.optionValues()), // 옵션 포맷팅
                            productInfo.basePrice() + productInfo.additionalPrice(),
                            productInfo.salePrice() + productInfo.additionalPrice(),
                            productInfo.quantity()
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

        // cartItems Id가 있을 경우 장바구니 비우기
        if (command.cartItemIds() != null && !command.cartItemIds().isEmpty()) {
            saveCartPort.deleteAllByMemberIdAndIds(command.memberId(), command.cartItemIds());
        }

        return orderResponseMapper.toCreateOrderResponse(savedOrder);
    }

    private Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 기본 배송지 조회 (없으면 null)
    private DeliveryAddressResponse getDefaultAddress(Long memberId) {
        return loadDeliveryAddressPort.findDefaultByMemberId(memberId)
                .map(deliveryAddressResponseMapper::toAddressResponse)
                .orElse(null);
    }

    private OrderCheckoutResponse createCheckoutResponse(Member member, DeliveryAddressResponse address, List<CheckoutItemDto> items) {
        long totalProductPrice = OrderPricePolicy.calculateTotalProductPrice(items);
        long membershipDiscount = OrderPricePolicy.calculateTotalMembershipDiscount(items);
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(items);

        return orderResponseMapper.toOrderCheckoutResponse(member, address, items, totalProductPrice, membershipDiscount, totalShippingFee);
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
