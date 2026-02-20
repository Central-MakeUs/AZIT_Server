package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleMonthlyListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.ParticipantResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.member.application.port.in.dto.CheckInStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CrewScheduleResponseMapper {
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    private static final int PARTICIPANT_PREVIEW_LIMIT = 10;
    private static final int CHECK_IN_COOL_DOWN_MINUTES = 30; // 출석 완료 후 최소 유지 시간
    private static final int ACTIVE_CHECK_IN_WINDOW_HOURS = 1; // 출석 버튼 활성화 윈도우 (전후 1시간)
    private static final int COMPLETED_RETENTION_HOURS = 3;    // 지난 일정 '완료' 표시 유지 시간

    public CrewScheduleDetailResponse toDetailResponse(
            CrewSchedule schedule, Long currentMemberId, Map<Long, MemberProfileDto> profileMap, Map<Long, CrewMember> crewMemberMap) {
        // 전체 참여자 정보를 조합하여 정렬
        List<ParticipantResponse> allParticipants = getAllSortedParticipants(schedule, profileMap, crewMemberMap);

        // 미리보기용으로 10명 추출
        List<ParticipantResponse> previewParticipants = allParticipants.stream()
                .limit(PARTICIPANT_PREVIEW_LIMIT)
                .toList();

        // 전체 인원이 10명보다 많으면 더보기 true
        boolean hasMoreParticipants = allParticipants.size() > PARTICIPANT_PREVIEW_LIMIT;

        return CrewScheduleDetailResponse.of(schedule, currentMemberId, previewParticipants, hasMoreParticipants);
    }

    public SliceResponse<ParticipantResponse> toParticipantSliceResponse(
            CrewSchedule schedule, Map<Long, MemberProfileDto> profileMap, Map<Long, CrewMember> crewMemberMap, CursorPageQuery query) {
        // 전체 참여자 정보를 조합하여 정렬
        List<ParticipantResponse> allParticipants = getAllSortedParticipants(schedule, profileMap, crewMemberMap);

        // 커서 위치 찾기 및 슬라이싱
        int startIndex = 0;
        if (query.cursorId() != null) {
            // cursorId와 일치하는 멤버의 다음 인덱스부터 시작
            startIndex = IntStream.range(0, allParticipants.size())
                    .filter(i -> allParticipants.get(i).memberId().equals(query.cursorId())) // cursorId가 몇번째 인덱스에 있는지 검색
                    .map(i -> i + 1) // 다음 인덱스를 시작점으로 설정
                    .findFirst()
                    .orElse(0); // cursorId 가 없을 경우 0으로 설정
        }

        int pageSize = query.size();
        int endIndex = Math.min(startIndex + pageSize, allParticipants.size());

        List<ParticipantResponse> content = allParticipants.subList(startIndex, endIndex);
        boolean hasNext = endIndex < allParticipants.size();

        // 마지막 아이템의 ID를 lastId로 반환
        Long lastId = content.isEmpty() ? null : content.getLast().memberId();

        return new SliceResponse<>(content, hasNext, lastId);
    }

    // 일정 상세 조회 및 참여자 조회 시 사용되는 공통 정렬 로직
    private List<ParticipantResponse> getAllSortedParticipants(
            CrewSchedule schedule, Map<Long, MemberProfileDto> profileMap, Map<Long, CrewMember> crewMemberMap) {

        return schedule.getParticipants().stream()
                // 데이터 정합성 검증 필터링
                .filter(participant -> profileMap.containsKey(participant.getMemberId()) && crewMemberMap.containsKey(participant.getMemberId()))
                .map(participant -> {
                    Long id = participant.getMemberId();
                    MemberProfileDto profile = profileMap.get(id);
                    CrewMember crewMember = crewMemberMap.get(id);

                    return ParticipantResponse.of(
                            id,
                            profile.nickname(),
                            imageUrlFormatUtil.buildFullImageUrl(profile.profileImageUrl()),
                            crewMember.getRole(),
                            id.equals(schedule.getCreatorId()),
                            participant.getCreatedAt()
                    );
                })
                // 일정 생성자 -> 리더 -> 신청 시간 순으로 정렬
                .sorted(Comparator.comparing((ParticipantResponse p) -> p.isCreator() ? 0 : 1)
                        .thenComparing(p -> p.role() == CrewMemberRole.LEADER ? 0 : 1)
                        .thenComparing(ParticipantResponse::participatedAt))
                .toList();
    }

    public List<CrewScheduleListResponse> toScheduleListResponse(List<CrewSchedule> schedules, Long currentMemberId) {
        return schedules.stream()
                .map(schedule -> CrewScheduleListResponse.of(schedule, currentMemberId))
                .toList();
    }

    public List<CrewScheduleMonthlyListResponse> toScheduleMonthlyListResponse(Map<LocalDate, Set<RunType>> monthlyScheduleMap) {
        return monthlyScheduleMap.entrySet().stream()
                .map(entry -> CrewScheduleMonthlyListResponse.of(
                        entry.getKey(),
                        entry.getValue().contains(RunType.REGULAR), // 정기런 존재 여부
                        entry.getValue().contains(RunType.LIGHTNING) // 번개런 존재 여부
                ))
                .sorted(Comparator.comparing(CrewScheduleMonthlyListResponse::date)) // 날짜순 정렬
                .toList();
    }

    public CheckInStatusResponse toCheckInStatusResponse(List<CrewSchedule> todaySchedules, Optional<CrewSchedule> nextSchedule, Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 오늘 일정이 있는 경우
        if (!todaySchedules.isEmpty()) {
            // 30분 이내에 출석을 완료한 경우, 다음 일정이 있더라도 출석 완료 상태를 30분간 노출
            // 출석하기 비활성화
            Optional<CrewSchedule> justCompletedSchedule = todaySchedules.stream()
                    .filter(s -> s.isCheckedIn(memberId))
                    .filter(s -> s.getMeetingAt().isBefore(now) &&
                            now.isBefore(s.getMeetingAt().plusMinutes(CHECK_IN_COOL_DOWN_MINUTES))) // 30분간 출석 완료 상태 유지
                    .findFirst();

            if (justCompletedSchedule.isPresent()) {
                return CheckInStatusResponse.of(
                        true,
                        CheckInStatusResponse.TodayScheduleResponse.of(justCompletedSchedule.get(), true, false),
                        null
                );
            }

            // 출석 가능하거나 곧 시작할 일정 필터링 (일정 시작 1시간 전후 기준)
            // 출석하기 활성화
            Optional<CrewSchedule> activeSchedule = todaySchedules.stream()
                    .filter(s -> !s.isCheckedIn(memberId))
                    .filter(s -> now.isAfter(s.getMeetingAt().minusHours(ACTIVE_CHECK_IN_WINDOW_HOURS)) &&
                            now.isBefore(s.getMeetingAt().plusHours(ACTIVE_CHECK_IN_WINDOW_HOURS))).min(Comparator.comparing(CrewSchedule::getMeetingAt)
                            .thenComparing(s -> s.getRunType() == RunType.REGULAR ? 0 : 1)); // 동일 시간대일 경우 정기런 우선 노출

            if (activeSchedule.isPresent()) {
                return CheckInStatusResponse.of(
                        true,
                        CheckInStatusResponse.TodayScheduleResponse.of(activeSchedule.get(), false, true),
                        null
                );
            }

            // 출석을 완료했고, 일정이 시작한지 3시간 이내인 일정 필터링
            // 출석 완료 활성화, 출석하기 비활성화
            Optional<CrewSchedule> recentlyCompletedSchedule = todaySchedules.stream()
                    .filter(s -> s.isCheckedIn(memberId))
                    .filter(s -> s.getMeetingAt().isBefore(now) &&
                            s.getMeetingAt().isAfter(now.minusHours(COMPLETED_RETENTION_HOURS)))
                    .max(Comparator.comparing(CrewSchedule::getMeetingAt));

            if (recentlyCompletedSchedule.isPresent()) {
                return CheckInStatusResponse.of(
                        true,
                        CheckInStatusResponse.TodayScheduleResponse.of(recentlyCompletedSchedule.get(), true, false),
                        null
                );
            }

            // 오늘 남은 일정 중 가장 빠른 일정 필터링
            // 출석하기 비활성화
            Optional<CrewSchedule> upcomingTodaySchedule = todaySchedules.stream()
                    .filter(s -> !s.isCheckedIn(memberId))
                    .filter(s -> s.getMeetingAt().isAfter(now))
                    .findFirst();

            if (upcomingTodaySchedule.isPresent()) {
                return CheckInStatusResponse.of(
                        true,
                        CheckInStatusResponse.TodayScheduleResponse.of(upcomingTodaySchedule.get(), false, false),
                        null
                );
            }
        }

        // 오늘 일정은 없고 다음 일정이 있는 경우
        if (nextSchedule.isPresent()) {
            CrewSchedule targetSchedule = nextSchedule.get();
            long daysLeft = ChronoUnit.DAYS.between(now.toLocalDate(), targetSchedule.getMeetingAt().toLocalDate());
            return CheckInStatusResponse.of(
                    false,
                    null,
                    CheckInStatusResponse.NextScheduleResponse.of(targetSchedule, daysLeft)
            );
        }

        // 아예 일정이 없는 경우
        return CheckInStatusResponse.of(false, null, null);
    }

}
