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
    private final String shippingInstruction; // 배송 요청사항
    private final long totalProductPrice;
    private final long totalShippingFee;
    private final long membershipDiscount;
    private final long usedPoints;
    private final long totalPaymentPrice;
    private final PaymentMethod paymentMethod;
    private final String depositorName;
    private final String courier; // 택배사
    private final String trackingNumber; // 운송장 번호
    private OrderStatus status;
    private final List<OrderItem> orderItems;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Order create(Long memberId, String orderNumber, OrderAddress address, String shippingInstruction,
                               long totalShippingFee, long usedPoints, PaymentMethod paymentMethod, String depositorName, List<OrderItem> orderItems) {

        long totalProductPrice = orderItems.stream()
                .mapToLong(OrderItem::getTotalBasePrice)
                .sum();

        long totalSalePrice = orderItems.stream()
                .mapToLong(OrderItem::getTotalSalePrice)
                .sum();

        long membershipDiscount = totalProductPrice - totalSalePrice;
        long totalPaymentPrice = totalSalePrice - usedPoints + totalShippingFee;

        return Order.builder()
                .memberId(memberId)
                .orderNumber(orderNumber)
                .address(address)
                .shippingInstruction(shippingInstruction)
                .totalProductPrice(totalProductPrice)
                .totalShippingFee(totalShippingFee)
                .membershipDiscount(membershipDiscount)
                .usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice)
                .paymentMethod(paymentMethod)
                .depositorName(depositorName)
                .status(OrderStatus.PENDING)
                .orderItems(orderItems)
                .build();
    }

    // 취소 가능한 상태인지 확인
    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.PAID;
    }

    // 주문 취소
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    // 주문번호 생성 (규칙: AZ + YYMMDD + 6자리 난수)
    public static String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int randomNumber = ThreadLocalRandom.current().nextInt(1000000);
        return String.format("AZ%s%04d", datePart, randomNumber);
    }
}
