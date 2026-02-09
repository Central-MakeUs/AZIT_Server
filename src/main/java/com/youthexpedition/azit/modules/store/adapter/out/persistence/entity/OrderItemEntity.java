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

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "product_name", nullable = false)
    private String productName;      // 구매 당시 상품명

    @Column(name = "option_description", nullable = false)
    private String optionDescription; // 구매 당시 옵션 정보 (예: 색상: 블랙 / 사이즈: L)

    @Column(name = "base_price", nullable = false)
    private long basePrice;              // 구매 당시 개당 판매가

    @Column(name = "sale_price", nullable = false)
    private long salePrice;              // 구매 당시 개당 할인가

    @Column(nullable = false)
    private int quantity;            // 구매 수량

    protected void setOrder(OrderEntity order) {
        this.order = order;
    }
}
