package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crew_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CrewMemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrewMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrewMemberStatus status;
}
