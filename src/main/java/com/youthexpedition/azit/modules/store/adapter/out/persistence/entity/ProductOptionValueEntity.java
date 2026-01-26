package com.youthexpedition.azit.modules.store.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option_value")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 제한 (JPA만 생성할 수 있도록)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더로만 생성하도록 강제
@Builder
public class ProductOptionValueEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ProductOptionGroupEntity group;

    @Column(name = "value", nullable = false, length = 100)
    private String value; // S, M, 240 등 옵션 값

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 화면 노출 순서
}
