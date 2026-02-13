package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.infrastructure.common.util.StringFormatUtil;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.store.application.port.in.dto.*;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import com.youthexpedition.azit.modules.store.domain.model.policy.PointPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderResponseMapper {

    @Value("${payment.bank.name}")
    private String bankName;

    @Value("${payment.bank.account-number}")
    private String accountNumber;

    @Value("${payment.bank.account-holder}")
    private String accountHolder;

    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public OrderCheckoutResponse toOrderCheckoutResponse(Member member, DeliveryAddressResponse address, List<CheckoutItemDto> items,
                                                         long totalProductPrice, long membershipDiscount, long totalShippingFee) {
        // 주문 상품 상세 목록 매핑
        List<OrderCheckoutResponse.CheckoutItemDetailResponse> itemResponses = items.stream()
                .map(this::toCheckoutItemResponse)
                .toList();

        // 결제 수단 목록 매핑
        List<OrderCheckoutResponse.PaymentMethodResponse> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(this::toPaymentMethodResponse)
                .toList();

        return OrderCheckoutResponse.of(
                address,
                itemResponses,
                DepositAccountInfoResponse.of(bankName, accountNumber, accountHolder, null, null),
                OrderCheckoutResponse.PointInfoResponse.of(member.getTotalPoints(), PointPolicy.MIN_POINT_USAGE, PointPolicy.POINT_UNIT),
                paymentMethods,
                OrderSummaryResponse.of(totalProductPrice, membershipDiscount, 0, totalShippingFee)
        );
    }

    private OrderCheckoutResponse.CheckoutItemDetailResponse toCheckoutItemResponse(CheckoutItemDto item) {
        return OrderCheckoutResponse.CheckoutItemDetailResponse.of(
                item.productId(),
                item.skuId(),
                item.brandName(),
                item.productName(),
                StringFormatUtil.formatOptionValues(item.optionValues()),
                imageUrlFormatUtil.buildFullImageUrl(item.imageUrl()),
                item.basePrice(),
                item.salePrice(),
                item.quantity(),
                (item.basePrice() + item.additionalPrice()) * item.quantity(),
                (item.salePrice() + item.additionalPrice()) * item.quantity()
        );
    }

    private OrderCheckoutResponse.PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod) {
        return OrderCheckoutResponse.PaymentMethodResponse.of(
                paymentMethod.getCode(),
                paymentMethod.getDescription(),
                paymentMethod.isEnabled()
        );
    }

    public CreateOrderResponse toCreateOrderResponse(Order order) {
        return CreateOrderResponse.of(
                StringFormatUtil.buildFullOrderNumber(order.getOrderNumber()),
                CreateOrderResponse.OrderDeliveryInfoResponse.of(
                        order.getAddress().getRecipientName(),
                        order.getAddress().getPhoneNumber(),
                        order.getAddress().getBaseAddress(),
                        order.getAddress().getDetailAddress()
                ),
                buildDepositAccountInfo(order),
                OrderSummaryResponse.of(
                        order.getTotalProductPrice(),
                        order.getMembershipDiscount(),
                        order.getUsedPoints(),
                        order.getTotalShippingFee()
                )
        );
    }

    public OrderDetailResponse toOrderDetailResponse(Order order) {
        OrderDetailResponse.OrderDetailDeliveryInfoResponse deliveryInfo = OrderDetailResponse.OrderDetailDeliveryInfoResponse.of(
                order.getAddress().getRecipientName(),
                order.getAddress().getPhoneNumber(),
                order.getAddress().getBaseAddress(),
                order.getAddress().getDetailAddress(),
                order.getShippingInstruction()
        );

        OrderDetailResponse.ShippingResponse shippingInfo = OrderDetailResponse.ShippingResponse.of(
                order.getCourier(),
                order.getTrackingNumber()
        );

        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.of(
                        item.getProductId(),
                        item.getSkuId(),
                        item.getBrandName(),
                        item.getProductName(),
                        item.getOptionDescription(),
                        imageUrlFormatUtil.buildFullImageUrl(item.getProductImageUrl()),
                        item.getTotalSalePrice(),
                        item.getQuantity()
                )).toList();

        var summary = OrderSummaryResponse.of(
                order.getTotalProductPrice(),
                order.getMembershipDiscount(),
                order.getUsedPoints(),
                order.getTotalShippingFee()
        );

        return OrderDetailResponse.of(
                order.getId(),
                order.getCreatedAt(),
                StringFormatUtil.buildFullOrderNumber(order.getOrderNumber()),
                order.getStatus(),
                deliveryInfo,
                buildDepositAccountInfo(order),
                shippingInfo,
                items,
                summary
        );
    }

    public OrderListResponse toOrderListResponse(Order order) {
        List<OrderItemResponse> itemSummaries = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.of(
                        item.getProductId(),
                        item.getSkuId(),
                        item.getBrandName(),
                        item.getProductName(),
                        item.getOptionDescription(),
                        imageUrlFormatUtil.buildFullImageUrl(item.getProductImageUrl()),
                        item.getSalePrice(),
                        item.getQuantity()
                )).toList();

        return OrderListResponse.of(
                order.getId(),
                order.getCreatedAt(),
                StringFormatUtil.buildFullOrderNumber(order.getOrderNumber()),
                order.getStatus(),
                itemSummaries
        );
    }

    // 결제수단이 무통장입금일 경우 계좌 정보 생성
    private DepositAccountInfoResponse buildDepositAccountInfo(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            return null;
        }

        // 입금 기한 계산
        LocalDateTime paymentDeadline = order.calculatePaymentDeadline();

        return DepositAccountInfoResponse.of(
                bankName,
                accountNumber,
                accountHolder,
                order.getDepositorName(),
                paymentDeadline
        );
    }
}