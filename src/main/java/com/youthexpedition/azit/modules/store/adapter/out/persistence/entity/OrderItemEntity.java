package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class OrderItemEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String productName;      // 구매 당시 상품명

    @Column(nullable = false)
    private String optionDescription; // 구매 당시 옵션 정보 (예: 색상: 블랙 / 사이즈: L)

    @Column(nullable = false)
    private long price;              // 구매 당시 개당 판매가

    @Column(nullable = false)
    private int quantity;            // 구매 수량

    protected void setOrder(OrderEntity order) {
        this.order = order;
    }
}
