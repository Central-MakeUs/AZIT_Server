package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_sku")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class ProductSkuEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "additional_price", nullable = false)
    private Long additionalPrice; // 옵션별 추가 금액

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity; // 재고 수량

    @Builder.Default
    @OneToMany(mappedBy = "productSku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSkuOptionEntity> skuOptions = new ArrayList<>();
}
