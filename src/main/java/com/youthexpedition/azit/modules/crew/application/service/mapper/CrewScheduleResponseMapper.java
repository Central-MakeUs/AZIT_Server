package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.util.image.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceLogResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceMonthlyListResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CrewScheduleResponseMapper {
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    private static final int PARTICIPANT_PREVIEW_LIMIT = 10;

    public CrewScheduleDetailResponse toDetailResponse(
            CrewSchedule schedule, Long currentMemberId, Map<Long, MemberProfileDto> profileMap, Map<Long, CrewMember> crewMemberMap) {
        // 전체 참여자 정보를 조합하여 정렬
        List<ParticipantResponse> allActiveParticipants = getAllSortedParticipants(schedule, profileMap, crewMemberMap);

        // 미리보기용으로 10명 추출
        List<ParticipantResponse> previewParticipants = allActiveParticipants.stream()
                .limit(PARTICIPANT_PREVIEW_LIMIT)
                .toList();

        // 전체 인원이 10명보다 많으면 더보기 true
        boolean hasMoreParticipants = allActiveParticipants.size() > PARTICIPANT_PREVIEW_LIMIT;

        Long creatorId = schedule.getCreatorId();
        // 앱 탈퇴 등으로 계정이 완전히 삭제된 경우 null 처리 (프론트에서 비식별화)
        String creatorNickname = null;
        String creatorProfileImageUrl = null;
        CrewMemberRole creatorRole = CrewMemberRole.MEMBER;

        // 크루 탈퇴/방출 여부와 무관하게 계정이 존재하면 프로필 그대로 노출
        if (profileMap.containsKey(creatorId)) {
            MemberProfileDto creatorProfile = profileMap.get(creatorId);
            creatorNickname = creatorProfile.nickname();
            creatorProfileImageUrl = imageUrlFormatUtil.buildFullImageUrl(creatorProfile.profileImageUrl());
            if (crewMemberMap.containsKey(creatorId)) {
                creatorRole = crewMemberMap.get(creatorId).getRole();
            }
        }

        return CrewScheduleDetailResponse.of(
                schedule, currentMemberId, creatorId, creatorNickname, creatorProfileImageUrl, creatorRole, previewParticipants, hasMoreParticipants, allActiveParticipants.size());
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

        return schedule.getParticipants().values().stream()
                // 데이터 정합성 검증 필터링
                .map(participant -> {
                    Long id = participant.getMemberId();
                    MemberProfileDto profile = profileMap.get(id);
                    CrewMember crewMember = crewMemberMap.get(id);

                    // 앱 탈퇴 등으로 계정이 완전히 삭제된 경우 비식별화 처리
                    // 크루 탈퇴/방출 상태(EXITED/EXPELLED)여도 계정이 존재하면 프로필 그대로 노출
                    if (profile == null || crewMember == null) {
                        return ParticipantResponse.of(
                                id, // 페이징 위해 살려둠
                                null,
                                null,
                                (crewMember != null) ? crewMember.getRole() : CrewMemberRole.MEMBER,
                                id.equals(schedule.getCreatorId()),
                                participant.getCreatedAt()
                        );
                    }

                    // 정상 데이터인 경우
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

    public List<CrewScheduleListResponse> toScheduleListResponse(List<CrewSchedule> schedules, Long currentMemberId, Map<Long, List<Long>> activeMemberIdsMap) {
        return schedules.stream()
                .map(schedule -> {
                    List<Long> activeIds = activeMemberIdsMap.getOrDefault(schedule.getId(), Collections.emptyList());
                    return CrewScheduleListResponse.of(schedule, currentMemberId, activeIds);
                })
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

    public CheckInStatusResponse toTodayScheduleCheckInStatus(CrewSchedule schedule, boolean isCheckedIn, LocalDateTime checkedInAt, boolean isAvailableTime) {
        return CheckInStatusResponse.of(
                true,
                CheckInStatusResponse.TodayScheduleResponse.of(schedule, isCheckedIn, checkedInAt, isAvailableTime),
                null
        );
    }

    public CheckInStatusResponse toNextScheduleCheckInStatus(CrewSchedule nextSchedule, long daysLeft) {
        return CheckInStatusResponse.of(
                false,
                null,
                CheckInStatusResponse.NextScheduleResponse.of(nextSchedule, daysLeft)
        );
    }

    public CheckInStatusResponse toEmptyScheduleCheckInStatus() {
        return CheckInStatusResponse.of(false, null, null);
    }

    public MyAttendanceLogResponse toMyAttendanceLogResponse(int attendanceCount, long totalPoints, List<MyAttendanceLogResponse.DailyAttendanceLog> attendanceLogs) {
        return MyAttendanceLogResponse.of(attendanceCount, totalPoints, attendanceLogs);
    }

    public List<MyAttendanceLogResponse.DailyAttendanceLog> toDailyAttendanceLogs(List<CrewSchedule> schedules, Long memberId) {
        return schedules.stream()
                .map(s -> {
                    boolean isCheckedIn = s.isCheckedIn(memberId);
                    AttendanceStatus status = isCheckedIn ? AttendanceStatus.ATTENDED : AttendanceStatus.ABSENT;

                    return MyAttendanceLogResponse.DailyAttendanceLog.of(
                            s.getId(),
                            s.getTitle(),
                            s.getRunType(),
                            s.getMeetingAt(),
                            s.getLocation().getPlaceName(),
                            status
                    );
                })
                .toList();
    }

    public List<MyAttendanceMonthlyListResponse> toMyAttendanceMonthlyListResponse(Map<LocalDate, Set<RunType>> attendanceMap) {
        return attendanceMap.entrySet().stream()
                .map(entry -> MyAttendanceMonthlyListResponse.of(
                        entry.getKey(),
                        entry.getValue().contains(RunType.REGULAR), // 정기런 존재 여부
                        entry.getValue().contains(RunType.LIGHTNING) // 번개런 존재 여부
                ))
                .sorted(Comparator.comparing(MyAttendanceMonthlyListResponse::date)) // 날짜순 정렬
                .toList();
    }

}
