package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.store.application.port.in.dto.*;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import com.youthexpedition.azit.modules.store.domain.model.PointPolicy;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class OrderResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    private static final String BANK_NAME = "신한은행";
    private static final String ACCOUNT_NUMBER = "110-322-291923";
    private static final String ACCOUNT_HOLDER = "성수립";
    private static final String ORDER_NUMBER_PREFIX = "#";

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
                DepositAccountInfoResponse.of(BANK_NAME, ACCOUNT_NUMBER, ACCOUNT_HOLDER, null, null),
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
                formatOptionValues(item.optionValues()),
                buildFullImageUrl(item.imageUrl()),
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
                buildFullOrderNumber(order.getOrderNumber()),
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
                        buildFullImageUrl(item.getProductImageUrl()),
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
                buildFullOrderNumber(order.getOrderNumber()),
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
                        buildFullImageUrl(item.getProductImageUrl()),
                        item.getSalePrice(),
                        item.getQuantity()
                )).toList();

        return OrderListResponse.of(
                order.getId(),
                order.getCreatedAt(),
                buildFullOrderNumber(order.getOrderNumber()),
                order.getStatus(),
                itemSummaries
        );
    }

    // 옵션 + / + 옵션 형식으로 조합하는 메서드
    public String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(" / ", optionValues);
    }

    private String buildFullImageUrl(String imagePath) {
        if (imagePath == null) return null;
        return cloudFrontDomain + imagePath;
    }

    private String buildFullOrderNumber(String orderNumber) {
        return ORDER_NUMBER_PREFIX + orderNumber;
    }

    // 결제수단이 무통장입금일 경우 계좌 정보 생성
    private DepositAccountInfoResponse buildDepositAccountInfo(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            return null;
        }

        LocalDateTime paymentDeadline = order.getCreatedAt().plusDays(1);

        return DepositAccountInfoResponse.of(
                BANK_NAME,
                ACCOUNT_NUMBER,
                ACCOUNT_HOLDER,
                order.getDepositorName(),
                paymentDeadline
        );
    }
}