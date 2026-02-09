package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.modules.store.domain.model.enums.OrderStatus;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Builder
@AllArgsConstructor
public class Order {
    private final Long id;
    private final Long memberId;
    private final String orderNumber;
    private final OrderAddress address; // 배송지 정보 스냅샷
    private final long totalProductPrice;
    private final long totalDiscountPrice;
    private final long totalShippingFee;
    private final long usedPoints;
    private final long totalPaymentPrice;
    private final PaymentMethod paymentMethod;
    private OrderStatus status;
    private final List<OrderItem> orderItems;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Order create(Long memberId, String orderNumber, OrderAddress address, List<OrderItem> items) {
        return Order.builder()
                .memberId(memberId)
                .orderNumber(orderNumber)
                .address(address)
                .status(OrderStatus.PENDING)
                .orderItems(items)
                .build();
    }

    // 주문번호 생성 (규칙: AZ + YYMMDD + 4자리 난수)
    private static String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int randomNumber = ThreadLocalRandom.current().nextInt(10000);
        return String.format("#AZ%s%04d", datePart, randomNumber);
    }
}
