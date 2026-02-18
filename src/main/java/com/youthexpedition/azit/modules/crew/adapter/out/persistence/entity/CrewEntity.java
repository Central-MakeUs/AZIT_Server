package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewStatus;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CrewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CrewCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Region region;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "invitation_code", nullable = false, unique = true, length = 20)
    private String invitationCode;

    @Builder.Default
    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrewStatus status;
}
