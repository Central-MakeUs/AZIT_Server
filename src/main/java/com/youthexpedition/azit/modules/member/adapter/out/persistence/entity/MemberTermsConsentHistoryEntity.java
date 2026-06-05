package com.youthexpedition.azit.modules.member.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_terms_consent_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberTermsConsentHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "terms_version_id", nullable = false)
    private Long termsVersionId;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
