package com.youthexpedition.azit.modules.member.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.member.domain.model.enums.TermsType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "terms_version",
        uniqueConstraints = @UniqueConstraint(name = "uq_terms_version", columnNames = {"terms_type", "version"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TermsVersionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 30)
    private TermsType termsType;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;
}
