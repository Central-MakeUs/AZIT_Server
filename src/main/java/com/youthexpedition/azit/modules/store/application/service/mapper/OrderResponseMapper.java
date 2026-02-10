package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CreateOrderResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import com.youthexpedition.azit.modules.store.domain.model.PointPolicy;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    private static final String BANK_NAME = "신한은행";
    private static final String ACCOUNT_NUMBER = "123-456-789012";
    private static final String ACCOUNT_HOLDER = "(주)아지트크루";

    public OrderCheckoutResponse toOrderCheckoutResponse(Member member, DeliveryAddressResponse address, List<CheckoutItemDto> items,
                                                         long totalProductPrice, long membershipDiscount, long totalShippingFee) {
        // 주문 상품 상세 목록 매핑
        List<OrderCheckoutResponse.CheckoutItemResponse> itemResponses = items.stream()
                .map(this::toCheckoutItemResponse)
                .toList();

        // 결제 수단 목록 매핑
        List<OrderCheckoutResponse.PaymentMethodResponse> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(this::toPaymentMethodResponse)
                .toList();

        return OrderCheckoutResponse.of(
                address,
                itemResponses,
                OrderCheckoutResponse.PointInfoResponse.of(member.getTotalPoints(), PointPolicy.MIN_POINT_USAGE, PointPolicy.POINT_UNIT),
                paymentMethods,
                OrderCheckoutResponse.CheckoutSummaryResponse.of(totalProductPrice, membershipDiscount, totalShippingFee)
        );
    }

    private OrderCheckoutResponse.CheckoutItemResponse toCheckoutItemResponse(CheckoutItemDto item) {
        String fullImageUrl = (item.imageUrl() != null) ? cloudFrontDomain + item.imageUrl() : null;

        return OrderCheckoutResponse.CheckoutItemResponse.of(
                item.brandName(),
                item.productName(),
                formatOptionValues(item.optionValues()),
                fullImageUrl,
                (item.basePrice() + item.additionalPrice()) * item.quantity(),
                (item.salePrice() + item.additionalPrice()) * item.quantity(),
                item.quantity()
        );
    }

    private OrderCheckoutResponse.PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod) {
        return OrderCheckoutResponse.PaymentMethodResponse.of(
                paymentMethod.getCode(),
                paymentMethod.getDescription(),
                paymentMethod.isEnabled()
        );
    }

    // 옵션 + / + 옵션 형식으로 조합하는 메서드
    public String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(" / ", optionValues);
    }

    public CreateOrderResponse toCreateOrderResponse(Order order) {
        // 결제 수단이 무통장 입금일 경우 계좌 정보 생성
        CreateOrderResponse.DepositAccountResponse depositAccount = null;
        if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            depositAccount = CreateOrderResponse.DepositAccountResponse.of(
                    BANK_NAME,
                    ACCOUNT_NUMBER,
                    ACCOUNT_HOLDER
            );
        }

        return CreateOrderResponse.of(
                order.getOrderNumber(),
                CreateOrderResponse.DeliveryAddressResponse.of(
                        order.getAddress().getRecipientName(),
                        order.getAddress().getPhoneNumber(),
                        order.getAddress().getBaseAddress(),
                        order.getAddress().getDetailAddress()
                ),
                depositAccount,
                CreateOrderResponse.CheckoutSummaryResponse.of(
                        order.getTotalProductPrice(),
                        order.getMembershipDiscount(),
                        order.getUsedPoints(),
                        order.getTotalShippingFee()
                )
        );
    }
}