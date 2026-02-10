package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.store.domain.model.enums.OrderStatus;
import com.youthexpedition.azit.modules.store.domain.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class OrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId; // 주문자 ID

    @Column(nullable = false, unique = true, length = 20)
    private String orderNumber; // 주문 번호 (예: AZ202602091234)

    // 배송지 정보 스냅샷 (주소 수정 시에도 주문 당시 주소 유지를 위함)
    @Column(name = "recipient_name", nullable = false, length = 50)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "base_address", nullable = false, length = 255)
    private String baseAddress;

    @Column(name = "detail_address", nullable = false, length = 255)
    private String detailAddress;

    @Column(name = "shipping_instruction", length = 100)
    private String shippingInstruction; // 배송 요청사항

    // 결제 금액 정보
    @Column(name = "total_product_price", nullable = false)
    private long totalProductPrice;   // 총 상품금액 (할인 전 합계)

    @Column(name = "total_shipping_fee", nullable = false)
    private long totalShippingFee;    // 총 배송비

    @Column(name = "membership_discount", nullable = false)
    private long membershipDiscount; // 아지트 멤버십 할인

    @Column(name = "used_points", nullable = false)
    private long usedPoints;          // 사용한 포인트

    @Column(name = "total_payment_price", nullable = false)
    private long totalPaymentPrice;       // 최종 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod; // 결제 수단

    @Column(name = "courier", length = 50)
    private String courier; // 택배사

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber; // 운송장 번호

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // 주문 상태

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItemEntity orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
