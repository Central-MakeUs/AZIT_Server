package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crew_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CrewScheduleEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "title", nullable = false, length = 15)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false, length = 20)
    private RunType runType;

    @Column(name = "meeting_at", nullable = false)
    private LocalDateTime meetingAt;

    @Embedded
    private LocationEntity locationEntity;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "pace", nullable = false)
    private Double pace;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewScheduleSupplyEntity> supplies = new ArrayList<>();

    public void addSupply(CrewScheduleSupplyEntity supply) {
        this.supplies.add(supply);
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;
}
