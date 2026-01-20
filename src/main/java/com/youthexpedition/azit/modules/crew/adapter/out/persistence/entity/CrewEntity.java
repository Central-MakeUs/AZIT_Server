package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CrewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CrewCategory category;

    @Column(nullable = false, length = 100)
    private Region region;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "invitation_code", nullable = false, unique = true, length = 20)
    private String invitationCode;
}
