package com.youthexpedition.azit.modules.member.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_terms_consent",
        uniqueConstraints = @UniqueConstraint(name = "uq_member_terms_version", columnNames = {"member_id", "terms_version_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberTermsConsentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "terms_version_id", nullable = false)
    private Long termsVersionId;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;
}
