package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.store.application.port.in.dto.OrderCheckoutResponse;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
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

    public OrderCheckoutResponse toOrderCheckoutResponse(Member member, DeliveryAddressResponse address, List<CartItemQueryDto> items, long shippingFee) {
        // 주문 상품 상세 목록 매핑
        List<OrderCheckoutResponse.CheckoutItemResponse> itemResponses = items.stream()
                .map(this::toCheckoutItemResponse)
                .toList();

        // 결제 수단 목록 매핑
        List<OrderCheckoutResponse.PaymentMethodResponse> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(this::toPaymentMethodResponse)
                .toList();

        // 전체 상품 금액, 할인 금액 계산
        long totalProductPrice = 0;
        long totalSalePrice = 0;
        for (CartItemQueryDto item : items) {
            totalProductPrice += (item.basePrice() + item.additionalPrice()) * item.quantity();
            totalSalePrice += (item.salePrice() + item.additionalPrice()) * item.quantity();
        }

        // 멤버십 할인액 계산 (정가 - 판매가)
        long membershipDiscount = totalProductPrice - totalSalePrice;

        return OrderCheckoutResponse.of(
                address,
                itemResponses,
                OrderCheckoutResponse.PointInfoResponse.of(member.getTotalPoints(), PointPolicy.MIN_POINT_USAGE, PointPolicy.POINT_UNIT),
                paymentMethods,
                OrderCheckoutResponse.CheckoutSummaryResponse.of(totalProductPrice, membershipDiscount, shippingFee)
        );
    }

    private OrderCheckoutResponse.CheckoutItemResponse toCheckoutItemResponse(CartItemQueryDto item) {
        String fullImageUrl = (item.imageUrl() != null) ? cloudFrontDomain + item.imageUrl() : null;

        return OrderCheckoutResponse.CheckoutItemResponse.of(
                item.cartItemId(),
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
    private String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(" / ", optionValues);
    }
}