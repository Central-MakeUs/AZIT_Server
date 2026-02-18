package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CrewSchedule {
    private final Long id;
    private final Long crewId;
    private final Long creatorId;
    private String title;
    private RunType runType;
    private LocalDateTime meetingAt;
    private Location location;
    private String description;
    private Double distance;        // 목표 거리
    private Double pace;            // 목표 페이스
    private Integer maxParticipants; // 최대 인원
    private List<String> supplies;  // 준비물 리스트
    private ScheduleStatus status;

    private static final int MAX_TITLE_LENGTH = 15;
    private static final int MAX_SUPPLY_COUNT = 5;

    public static CrewSchedule create(Long crewId, Long creatorId, String title, RunType runType,
                                      LocalDateTime meetingAt, Location location, String description,
                                      Double distance, Double pace, Integer maxParticipants, List<String> supplies
    ) {
        return CrewSchedule.builder()
                .crewId(crewId)
                .creatorId(creatorId)
                .title(title)
                .runType(runType)
                .meetingAt(meetingAt)
                .location(location)
                .description(description)
                .distance(distance)
                .pace(pace)
                .maxParticipants(maxParticipants)
                .supplies(supplies)
                .status(ScheduleStatus.ACTIVE)
                .build();
    }

    // 타이틀 15자 제한 검증
    public boolean isValidTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= MAX_TITLE_LENGTH;
    }

    // 준비물 최대 5개 검증
    public boolean isSuppliesCountValid(List<String> supplies) {
        return supplies == null || supplies.size() <= MAX_SUPPLY_COUNT;
    }

    // 현재보다 과거 시간인지 검증
    public boolean isMeetingTimeValid(LocalDateTime meetingAt) {
        return meetingAt != null && meetingAt.isAfter(LocalDateTime.now());
    }

    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }
}
