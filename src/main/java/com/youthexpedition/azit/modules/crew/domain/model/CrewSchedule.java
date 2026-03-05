package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

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
    private Integer distance;        // 목표 거리
    private Integer pace;            // 목표 페이스
    private Integer maxParticipants; // 최대 인원
    private List<String> supplies;  // 준비물 리스트
    @Builder.Default
    private final SequencedMap<Long, CrewScheduleMember> participants = new LinkedHashMap<>(); // 참여 인원, 순서가 있는 map 사용하여 성능 향상
    private ScheduleStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CrewSchedule create(Long crewId, Long creatorId, String title, RunType runType,
                                      LocalDateTime meetingAt, Location location, String description,
                                      Integer distance, Integer pace, Integer maxParticipants, List<String> supplies
    ) {
        CrewSchedule schedule = CrewSchedule.builder()
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

        schedule.addParticipant(creatorId); // 참여 인원에 작성자 추가
        return schedule;
    }

    // 현재보다 과거 시간인지 검증
    public boolean isMeetingTimeValid() {
        return this.meetingAt != null && this.meetingAt.isAfter(LocalDateTime.now());
    }

    // 일정 수정
    public void update(
            String title, RunType runType, LocalDateTime meetingAt, Location location,
            String description, Integer distance, Integer pace, Integer maxParticipants, List<String> supplies
    ) {
        this.title = title;
        this.runType = runType;
        this.meetingAt = meetingAt;
        this.location = location;
        this.description = description;
        this.distance = distance;
        this.pace = pace;
        this.maxParticipants = maxParticipants;
        this.supplies = supplies;
    }

    // 일정 취소
    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return this.status == ScheduleStatus.CANCELLED;
    }

    // 최대 인원 확인
    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }

    // 일정에 참여하고 있는지 확인
    public boolean isParticipating(Long memberId) {
        return participants.containsKey(memberId);
    }

    // 일정 참여
    public void addParticipant(Long memberId) {
        participants.put(memberId, CrewScheduleMember.builder()
                .memberId(memberId)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 일정 참여 취소
    public void removeParticipant(Long memberId) {
        participants.remove(memberId);
    }

    // 참여자 존재 여부 확인
    public boolean hasNoParticipants() {
        return participants.isEmpty();
    }

    // ID 리스트만 필요할 때 사용
    public List<Long> getParticipantIds() {
        return List.copyOf(participants.keySet());
    }

    // 특정 멤버의 출석 여부 확인
    public boolean isCheckedIn(Long memberId) {
        return Optional.ofNullable(participants.get(memberId))
                .map(CrewScheduleMember::isCheckedIn)
                .orElse(false);
    }

    // 출석 체크
    public void checkIn(Long memberId, LocalDateTime checkInTime) {
        participants.computeIfPresent(memberId, (id, m) ->
                CrewScheduleMember.builder()
                        .id(m.getId())
                        .memberId(m.getMemberId())
                        .isCheckedIn(true)
                        .checkedInAt(checkInTime)
                        .createdAt(m.getCreatedAt())
                        .build()
        );
    }
}
