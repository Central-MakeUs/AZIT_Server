package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.common.util.StringFormatUtil;
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
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderListResponse;
import com.youthexpedition.azit.modules.store.application.port.out.*;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.application.service.mapper.OrderResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import com.youthexpedition.azit.modules.store.domain.model.OrderAddress;
import com.youthexpedition.azit.modules.store.domain.model.OrderItem;
import com.youthexpedition.azit.modules.store.domain.model.enums.OrderType;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import com.youthexpedition.azit.modules.store.domain.model.policy.OrderPricePolicy;
import com.youthexpedition.azit.modules.store.domain.model.policy.PointPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
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
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderResponseMapper orderResponseMapper;
    private final DeliveryAddressResponseMapper deliveryAddressResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderCheckoutResponse getCheckoutInfoFromCart(Long memberId, List<Long> cartItemIds, Long deliveryAddressId) {
        Member member = getMember(memberId);

        // 기본 배송지 조회
        DeliveryAddressResponse address = getDeliveryAddress(memberId, deliveryAddressId);

        // 주문할 장바구니 아이템 상세 조회
        List<CheckoutItemDto> items = loadCartPort.findCartDetailsByIds(cartItemIds);

        // 재고 확인
        validateStockAvailability(items, memberId);

        return createCheckoutResponse(member, address, items);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCheckoutResponse getCheckoutInfoDirect(Long memberId, Long skuId, Integer quantity, Long deliveryAddressId) {
        Member member = getMember(memberId);

        // 기본 배송지 조회
        DeliveryAddressResponse address = getDeliveryAddress(memberId, deliveryAddressId);

        // 주문할 상품 상세 조회
        CheckoutItemDto item = loadProductPort.findProductInfoBySkuId(skuId, quantity)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.SKU_NOT_FOUND));

        // 재고 확인
        validateStockAvailability(List.of(item), memberId);

        return createCheckoutResponse(member, address, List.of(item));
    }

    @Override
    @Retryable(
            retryFor = {DataIntegrityViolationException.class}, // db 에러 시 재시도
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        // 포인트 사용 유효성 검증 및 회원 조회
        Member member = validatePointUsageAndGetMember(command.memberId(), command.usedPoints());

        // 결제 타입 유효성 체크
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(command.paymentMethod());
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 문자열일 경우 예외처리
            log.warn("[ORDER] memberId: {}, paymentMethod: {} 유효하지 않은 결제 수단입니다.", member.getId(), command.paymentMethod());
            throw new BusinessException(StoreErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }

        // 무통장 입금 시 입금자명 필수 검증
        if (paymentMethod == PaymentMethod.BANK_TRANSFER && (command.depositorName() == null || command.depositorName().isBlank())) {
                throw new BusinessException(StoreErrorCode.INVALID_ORDER_REQUEST);
            }

        // 결제 타입별로 상품 아이템 조회
        List<CheckoutItemDto> items = switch (OrderType.from(command)) {
            case CART -> loadCartPort.findCartDetailsByIds(command.cartItemIds());

            case DIRECT -> List.of(loadProductPort.findProductInfoBySkuId(command.skuId(), command.quantity())
                    .orElseThrow(() -> new BusinessException(StoreErrorCode.SKU_NOT_FOUND)));

            case INVALID -> throw new BusinessException(StoreErrorCode.INVALID_ORDER_REQUEST);
        };

        // 결제할 상품이 하나도 없는 경우 예외 처리
        if (items.isEmpty()) {
            throw new BusinessException(StoreErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        // 배송비 계산
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(items);

        // 결제 수단 확인 및 처리
        handlePayment(paymentMethod, member.getId());

        // 주문 번호 중복 방어
        String orderNumber = generateUniqueOrderNumber();

        List<OrderItem> orderItems = items.stream()
                .map(productInfo -> {
                    // 수량이 0보다 큰지 확인
                    if (!OrderItem.isValidQuantity(productInfo.quantity())) {
                        throw new BusinessException(StoreErrorCode.INVALID_QUANTITY);
                    }

                    // 재고 차감
                    saveProductPort.decreaseStock(productInfo.skuId(), productInfo.quantity());

                    return OrderItem.create(
                            productInfo.productId(),
                            productInfo.skuId(),
                            productInfo.brandName(),
                            productInfo.productName(),
                            productInfo.imageUrl(),
                            StringFormatUtil.formatOptionValues(productInfo.optionValues()),
                            productInfo.basePrice() + productInfo.additionalPrice(),
                            productInfo.salePrice() + productInfo.additionalPrice(),
                            productInfo.quantity()
                    );
                }).toList();

        Order order = Order.create(
                command.memberId(),
                orderNumber,
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
                command.depositorName(),
                orderItems
        );

        Order savedOrder = saveOrderPort.save(order);

        // 포인트 검증
        if (!member.hasEnoughPoints(command.usedPoints())) {
            throw new BusinessException(MemberErrorCode.INSUFFICIENT_POINTS);
        }
        // 포인트 차감
        member.deductPoints(command.usedPoints());
        saveMemberPort.save(member);

        // cartItems Id가 있을 경우 장바구니 비우기
        if (command.cartItemIds() != null && !command.cartItemIds().isEmpty()) {
            saveCartPort.deleteAllByMemberIdAndIds(command.memberId(), command.cartItemIds());
        }

        return orderResponseMapper.toCreateOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long memberId, String orderNumber) {
        Order order = loadOrderPort.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.ORDER_NOT_FOUND));

        // 본인의 주문만 조회 가능
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }

        return orderResponseMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<OrderListResponse> getOrders(Long memberId, CursorPageQuery query) {
        SliceResponse<Order> orderSlice = loadOrderPort.findOrdersByMemberId(memberId, query);

        List<OrderListResponse> responses = orderSlice.content().stream()
                .map(orderResponseMapper::toOrderListResponse)
                .toList();

        return new SliceResponse<>(responses, orderSlice.hasNext(), orderSlice.lastId());
    }

    @Override
    @Transactional
    public void cancelOrder(Long memberId, String orderNumber) {
        Order order = loadOrderPort.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.ORDER_NOT_FOUND));

        // 로그인한 사용자 주문인지 확인
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }

        // 주문 취소 가능한 상태인지 확인
        if (!order.isCancellable()) {
            throw new BusinessException(StoreErrorCode.CANNOT_CANCEL_ORDER);
        }
        // 주문 취소 상태로 변경
        order.cancel();

        // 재고 복구
        order.getOrderItems().forEach(item ->
                saveProductPort.increaseStock(item.getSkuId(), item.getQuantity())
        );

        // 포인트 환불
        if (order.getUsedPoints() > 0) {
            Member member = getMember(memberId);

            log.info("[ORDER] memberId: {}, usePoints: {} 포인트를 환불합니다.", memberId, order.getUsedPoints());
            member.addPoints(order.getUsedPoints()); // 사용한 포인트만큼 복구
            saveMemberPort.save(member);
        }

        saveOrderPort.save(order);
    }

    private Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 배송지 조회
    private DeliveryAddressResponse getDeliveryAddress(Long memberId, Long deliveryAddressId) {
        if (deliveryAddressId != null) {
            return loadDeliveryAddressPort.findById(deliveryAddressId)
                    .map(deliveryAddressResponseMapper::toAddressResponse)
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
        }
        // deliveryAddressId가 없으면 기본 배송지 조회
        return getDefaultAddress(memberId);
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

    // 포인트 사용 유효성 검증 및 사용자 반환
    public Member validatePointUsageAndGetMember(Long memberId, long usePoints) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        log.info("[ORDER] memberId: {}, usePoints: {} 포인트를 사용합니다.", memberId, usePoints);
        PointPolicy.validate(member, usePoints);

        return member;
    }

    // 결제 수단별 처리
    private void handlePayment(PaymentMethod paymentMethod, Long memberId) {
        if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
            // 무통장 입금은 별도 API 연동 없이 주문 승인 대기 상태로 진행
            return;
        }

        // 지원하지 않는 결제수단 체크
        if (!paymentMethod.isEnabled()) {
            log.warn("[ORDER] memberId: {}, paymentMethod: {} 지원하지 않는 결제 수단입니다.", memberId, paymentMethod);
            throw new BusinessException(StoreErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
    }

    private String generateUniqueOrderNumber() {
        final int MAX_RETRIES = 10;

        for (int i = 0; i < MAX_RETRIES; i++) {
            String orderNumber = Order.generateOrderNumber();

            // DB 조회하여 중복 여부 확인
            if (!loadOrderPort.existsByOrderNumber(orderNumber)) {
                return orderNumber;
            }
        }
        // 최대 재시도 후에도 실패 시 예외 발생
        log.error("[ORDER] 주문 번호 생성 최대 재시도 횟수({})를 초과했습니다.", MAX_RETRIES);
        throw new BusinessException(StoreErrorCode.ORDER_NUMBER_GENERATION_FAILED);
    }

    private void validateStockAvailability(List<CheckoutItemDto> items, Long memberId) {
        for (CheckoutItemDto item : items) {
            // 주문 수량이 유효한지 체크
            if (!OrderItem.isValidQuantity(item.quantity())) {
                log.warn("[ORDER] memberId: {}, productId: {}, skuId: {}, 요청 수량: {} 유효하지 않은 수량입니다.", memberId, item.productId(), item.skuId(), item.quantity());
                throw new BusinessException(StoreErrorCode.INVALID_QUANTITY);
            }

            // sku의 현재 재고보다 주문 요청 수량이 많은지 체크
            if (item.stockQuantity() < item.quantity()) {
                log.warn("[ORDER] memberId: {}, productId: {}, skuId: {}, 요청 수량: {} 재고가 부족합니다.", memberId, item.productId(), item.skuId(), item.quantity());
                throw new BusinessException(StoreErrorCode.OUT_OF_STOCK);
            }
        }
    }

}
