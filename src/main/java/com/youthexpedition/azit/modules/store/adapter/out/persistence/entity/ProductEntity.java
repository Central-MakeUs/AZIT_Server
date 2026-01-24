package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "base_price", nullable = false)
    private Long basePrice;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Column(name = "sale_price", nullable = false)
    private Long salePrice;

    @Column(name = "shipping_fee", nullable = false)
    private Long shippingFee;

    @Column(name = "shipping_policy", length = 500)
    private String shippingPolicy;

    @Column(name = "refund_policy", length = 1000)
    private String refundPolicy;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImageEntity> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSkuEntity> skus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionGroupEntity> optionGroups = new ArrayList<>();
}
