package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import com.youthexpedition.azit.infrastructure.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_schedule_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CrewScheduleMemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private CrewScheduleEntity schedule;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "is_checked_in", nullable = false)
    @Builder.Default
    private boolean isCheckedIn = false;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    public void syncCheckIn(boolean isCheckedIn, LocalDateTime checkedInAt) {
        this.isCheckedIn = isCheckedIn;
        this.checkedInAt = checkedInAt;
    }
}
