package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_option_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class ProductOptionGroupEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "name", nullable = false, length = 50)
    private String name; // 컬러, 사이즈 등 옵션 그룹

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 화면 노출 순서

    @Builder.Default
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionValueEntity> values = new ArrayList<>();
}
