package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.common.util.LocationDistanceUtil;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.*;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleMonthlyQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleQuery;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewSchedulePort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.application.service.dto.ScheduleData;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewScheduleResponseMapper;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.Location;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceLogResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceMonthlyListResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.port.query.MyAttendanceMonthlyQuery;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrewScheduleService implements CrewScheduleUseCase {
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewSchedulePort loadCrewSchedulePort;
    private final SaveCrewSchedulePort saveCrewSchedulePort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final CrewScheduleResponseMapper crewScheduleResponseMapper;

    private static final int ACTIVE_CHECK_IN_WINDOW_HOURS = 1; // 출석 버튼 활성화 윈도우 (전후 1시간)
    private static final int COMPLETED_RETENTION_HOURS = 3;    // 지난 일정 완료 표시 유지 시간
    private static final long CHECK_IN_POINTS = 100L;
    private static final double CHECK_IN_AVAILABLE_DISTANCE_METERS = 1000.0;

    @Override
    public void createSchedule(CreateScheduleCommand command) {
        // 크루 정회원인지 확인
        CrewMember creator = getJoinedMember(command.crewId(), command.creatorId());

        // 정기런은 리더만 생성 가능
        if (command.runType() == RunType.REGULAR && creator.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.ONLY_LEADER_CAN_CREATE_REGULAR_RUN);
        }

        Location location = Location.builder()
                .placeName(command.placeName())
                .address(command.address())
                .meetingSpot(command.meetingSpot())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .build();

        CrewSchedule crewSchedule = CrewSchedule.create(
                command.crewId(),
                command.creatorId(),
                command.title(),
                command.runType(),
                command.meetingAt(),
                location,
                command.description(),
                command.distance(),
                command.pace(),
                command.maxParticipants(),
                command.supplies()
        );

        // 유효성 체크
        validateSchedule(crewSchedule);
        validateScheduleInterval(command.creatorId(), command.meetingAt());
        saveCrewSchedulePort.save(crewSchedule);
    }

    @Override
    public void updateSchedule(UpdateScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 본인이 생성한 일정인지 확인
        validateCreator(schedule, command.creatorId());

        // 출석이 가능한 시간인 경우 수정 및 삭제 불가
        if (!schedule.isModifiable(LocalDateTime.now())) {
            throw new BusinessException(CrewErrorCode.SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME);
        }

        // 크루 정회원인지 확인
        CrewMember creator = getJoinedMember(command.crewId(), command.creatorId());

        // 정기런은 리더만 생성 가능
        if (command.runType() == RunType.REGULAR && creator.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.ONLY_LEADER_CAN_CREATE_REGULAR_RUN);
        }

        Location location = Location.builder()
                .placeName(command.placeName())
                .address(command.address())
                .meetingSpot(command.meetingSpot())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .build();

        schedule.update(
                command.title(), command.runType(), command.meetingAt(), location,
                command.description(), command.distance(), command.pace(),
                command.maxParticipants(), command.supplies()
        );

        // 유효성 체크
        validateSchedule(schedule);
        validateScheduleInterval(command.creatorId(), command.meetingAt(), schedule.getId());
        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void cancelSchedule(CancelScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 이미 취소된 일정인지 확인
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }

        // 본인이 생성한 일정인지 확인
        validateCreator(schedule, command.creatorId());

        // 출석이 가능한 시간인 경우 수정 및 삭제 불가
        if (!schedule.isModifiable(LocalDateTime.now())) {
            throw new BusinessException(CrewErrorCode.SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME);
        }

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.creatorId());

        // 삭제 처리
        schedule.cancel();

        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void participateSchedule(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        // 일정 참여 가능한지 검증
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }
        if (schedule.isParticipating(command.memberId())) {
            throw new BusinessException(CrewErrorCode.ALREADY_PARTICIPATED);
        }
        if (schedule.isFull()) {
            throw new BusinessException(CrewErrorCode.EXCEEDED_MAX_PARTICIPANTS);
        }
        if (!schedule.isParticipationModifiable(LocalDateTime.now())) {
            throw new BusinessException(CrewErrorCode.PARTICIPATION_AND_CANCEL_CLOSED);
        }

        // 기존 일정과의 시간 간격 검증
        validateScheduleInterval(command.memberId(), schedule.getMeetingAt());

        schedule.addParticipant(command.memberId());
        saveCrewSchedulePort.save(schedule);
    }

    @Override
    public void cancelParticipation(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 참여 중인지 확인
        if (!schedule.isParticipating(command.memberId())) {
            throw new BusinessException(CrewErrorCode.NOT_PARTICIPATING_SCHEDULE);
        }
        // 본인이 일정 생성자인지 확인
        if (schedule.getCreatorId().equals(command.memberId())) {
            throw new BusinessException(CrewErrorCode.CREATOR_CANNOT_CANCEL_PARTICIPATION);
        }
        if (schedule.isCheckedIn(command.memberId())) {
            throw new BusinessException(CrewErrorCode.CANNOT_CANCEL_AFTER_CHECK_IN);
        }
        if (!schedule.isParticipationModifiable(LocalDateTime.now())) {
            throw new BusinessException(CrewErrorCode.PARTICIPATION_AND_CANCEL_CLOSED);
        }

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        schedule.removeParticipant(command.memberId());
        if (schedule.hasNoParticipants()) schedule.cancel(); // 참여 취소 후 해당 일정에 참여자가 아무도 없으면 일정 취소

        saveCrewSchedulePort.save(schedule);
    }

    @Transactional(readOnly = true)
    @Override
    public CrewScheduleDetailResponse getScheduleDetail(CrewScheduleCommand command) {
        ScheduleData data = getValidatedScheduleData(command);

        return crewScheduleResponseMapper.toDetailResponse(
                data.schedule(), command.memberId(), data.profileMap(), data.crewMemberMap());
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ParticipantResponse> getScheduleParticipants(CrewScheduleCommand command, CursorPageQuery query) {
        ScheduleData data = getValidatedScheduleData(command);

        return crewScheduleResponseMapper.toParticipantSliceResponse(
                data.schedule(), data.profileMap(), data.crewMemberMap(), query);
    }

    // 일정 상세 및 참여자 명단 조회 시 공통적으로 사용하는 검증 및 데이터 로딩 로직
    private ScheduleData getValidatedScheduleData(CrewScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 취소된 일정인지 확인
        if (schedule.isCancelled()) {
            throw new BusinessException(CrewErrorCode.ALREADY_CANCELLED_SCHEDULE);
        }

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        Set<Long> participantIds = new HashSet<>(schedule.getParticipantIds());
        participantIds.add(schedule.getCreatorId()); // 생성자 ID까지 조회 (생성자가 참여하고 있지 않은 경우)

        Map<Long, MemberProfileDto> profileMap = loadMemberPort.findAllByIds(new ArrayList<>(participantIds));
        Map<Long, CrewMember> crewMemberMap = loadCrewMemberPort.findAllByCrewIdAndMemberIds(command.crewId(), new ArrayList<>(participantIds));

        // 데이터 정합성 검증
        if (profileMap.size() != participantIds.size() || crewMemberMap.size() != participantIds.size()) {
            log.warn("[CREW_SCHEDULE] scheduleId: {} 의 데이터 정합성이 맞지 않습니다. 기대 참여자 수: {}, 조회된 프로필 수: {}, 조회된 크루 멤버 수: {}",
                    schedule.getId(), participantIds.size(), profileMap.size(), crewMemberMap.size());
        }

        return new ScheduleData(schedule, profileMap, crewMemberMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleListResponse> getSchedules(CrewScheduleQuery query) {
        // 정회원인지 확인
        getJoinedMember(query.crewId(), query.memberId());

        // 필터링된 일정 목록 조회
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByFilter(query.crewId(), query.date(), query.yearMonth(), query.runType());

        List<Long> allParticipantIds = schedules.stream()
                .flatMap(s -> s.getParticipantIds().stream()).distinct().toList();
        Map<Long, MemberProfileDto> activeProfiles = loadMemberPort.findAllByIds(allParticipantIds);

        // 탈퇴한 멤버는 제외하고 조회
        Map<Long, List<Long>> activeMemberIdsMap = schedules.stream()
                .collect(Collectors.toMap(CrewSchedule::getId,
                        s -> s.getParticipantIds().stream().filter(activeProfiles::containsKey).toList()));

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, query.memberId(), activeMemberIdsMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleMonthlyListResponse> getMonthlySchedulesForCalendar(CrewScheduleMonthlyQuery query) {
        // 정회원인지 확인
        getJoinedMember(query.crewId(), query.memberId());

        Map<LocalDate, Set<RunType>> monthlyScheduleMap = loadCrewSchedulePort.findMonthlySchedulesForCalendar(query.crewId(), query.yearMonth());

        return crewScheduleResponseMapper.toScheduleMonthlyListResponse(monthlyScheduleMap);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrewScheduleListResponse> getMySchedules(Long memberId) {
        // 참여 중인 일정 조회
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByMemberId(memberId);

        List<Long> allParticipantIds = schedules.stream()
                .flatMap(s -> s.getParticipantIds().stream()).distinct().toList();
        Map<Long, MemberProfileDto> activeProfiles = loadMemberPort.findAllByIds(allParticipantIds);

        Map<Long, List<Long>> activeMemberIdsMap = schedules.stream()
                .collect(Collectors.toMap(CrewSchedule::getId,
                        s -> s.getParticipantIds().stream().filter(activeProfiles::containsKey).toList()));

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, memberId, activeMemberIdsMap);
    }

    @Transactional(readOnly = true)
    @Override
    public CheckInStatusResponse getCheckInStatus(Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 오늘 참여하는 모든 일정 조회
        List<CrewSchedule> todaySchedules = loadCrewSchedulePort.findAllTodaySchedulesByMemberId(memberId, now);
        // 가장 가까운 미래 일정 조회
        Optional<CrewSchedule> nextSchedule = loadCrewSchedulePort.findNextClosestScheduleByMemberId(memberId, now);

        // 출석을 완료한 일정 확인 (가장 최근 일정 기준, 과거/미래 상관x)
        Optional<CrewSchedule> checkedInScheduleOpt = todaySchedules.stream()
                .filter(s -> s.isCheckedIn(memberId))
                .max(Comparator.comparing(CrewSchedule::getMeetingAt));

        if (checkedInScheduleOpt.isPresent()) {
            CrewSchedule checkedInSchedule = checkedInScheduleOpt.get();

            // 이 일정 뒤에 이어지는 오늘의 다음 일정 찾기
            Optional<CrewSchedule> nextTodaySchedule = todaySchedules.stream()
                    .filter(s -> s.getMeetingAt().isAfter(checkedInSchedule.getMeetingAt()))
                    .min(Comparator.comparing(CrewSchedule::getMeetingAt));

            LocalDateTime retentionEndTime;

            if (nextTodaySchedule.isPresent()) {
                LocalDateTime nextCheckInOpenTime = nextTodaySchedule.get().getMeetingAt().minusHours(ACTIVE_CHECK_IN_WINDOW_HOURS);
                LocalDateTime defaultRetentionTime = checkedInSchedule.getMeetingAt().plusHours(COMPLETED_RETENTION_HOURS);

                // 기존 3시간 유지 시간이 다음 일정의 출석 오픈 시간과 겹치는지 확인
                if (!defaultRetentionTime.isBefore(nextCheckInOpenTime)) {
                    // 겹칠 경우 노출 시간을 1시간으로 변경
                    retentionEndTime = checkedInSchedule.getMeetingAt().plusHours(ACTIVE_CHECK_IN_WINDOW_HOURS);
                } else {
                    retentionEndTime = defaultRetentionTime;
                }
            } else {
                // 뒤에 일정이 없으면 기본 3시간 유지
                retentionEndTime = checkedInSchedule.getMeetingAt().plusHours(COMPLETED_RETENTION_HOURS);
            }

            // 계산된 유지 시간 이내라면 완료 상태 노출
            if (now.isBefore(retentionEndTime)) {
                LocalDateTime checkInTime = checkedInSchedule.getParticipants().get(memberId).getCheckedInAt();
                return crewScheduleResponseMapper.toTodayScheduleCheckInStatus(checkedInSchedule, true, checkInTime, false);
            }
        }

        // 출석 가능하거나 곧 시작할 일정 필터링 (일정 시작 1시간 전후 기준)
        // 출석하기 활성화
        Optional<CrewSchedule> activeSchedule = todaySchedules.stream()
                .filter(s -> !s.isCheckedIn(memberId))
                .filter(s -> now.isAfter(s.getMeetingAt().minusHours(ACTIVE_CHECK_IN_WINDOW_HOURS)) &&
                        now.isBefore(s.getMeetingAt().plusHours(ACTIVE_CHECK_IN_WINDOW_HOURS))).min(Comparator.comparing(CrewSchedule::getMeetingAt)
                        .thenComparing(s -> s.getRunType() == RunType.REGULAR ? 0 : 1)); // 동일 시간대일 경우 정기런 우선 노출

        if (activeSchedule.isPresent()) {
            return crewScheduleResponseMapper.toTodayScheduleCheckInStatus(activeSchedule.get(), false, null, true);
        }

        // 오늘 남은 일정 중 가장 빠른 일정 필터링 (출석 오픈 전)
        // 출석하기 비활성화
        Optional<CrewSchedule> upcomingTodaySchedule = todaySchedules.stream()
                .filter(s -> !s.isCheckedIn(memberId))
                .filter(s -> s.getMeetingAt().isAfter(now))
                .min(Comparator.comparing(CrewSchedule::getMeetingAt));

        if (upcomingTodaySchedule.isPresent()) {
            return crewScheduleResponseMapper.toTodayScheduleCheckInStatus(upcomingTodaySchedule.get(), false, null, false);
        }

        // 오늘 일정은 없고 다음 일정이 있는 경우
        if (nextSchedule.isPresent()) {
            CrewSchedule targetSchedule = nextSchedule.get();
            long daysLeft = ChronoUnit.DAYS.between(now.toLocalDate(), targetSchedule.getMeetingAt().toLocalDate());
            return crewScheduleResponseMapper.toNextScheduleCheckInStatus(nextSchedule.get(), daysLeft);
        }

        // 아예 일정이 없는 경우
        return crewScheduleResponseMapper.toEmptyScheduleCheckInStatus();
    }

    @Override
    public void checkInSchedule(CheckInCommand command) {
        LocalDateTime now = LocalDateTime.now();
        CrewSchedule schedule = getSchedule(command.scheduleId());
        Member member = loadMemberPort.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 시간 검증 (1시간 전후)
        if (now.isBefore(schedule.getMeetingAt().minusHours(ACTIVE_CHECK_IN_WINDOW_HOURS)) ||
                now.isAfter(schedule.getMeetingAt().plusHours(ACTIVE_CHECK_IN_WINDOW_HOURS))) {
            throw new BusinessException(CrewErrorCode.NOT_CHECK_IN_TIME);
        }

        // 거리 검증 (1000m 이내)
        double distance = LocationDistanceUtil.calculateDistance(
                schedule.getLocation().getLatitude(), schedule.getLocation().getLongitude(), command.latitude(), command.longitude());

        if (distance > CHECK_IN_AVAILABLE_DISTANCE_METERS) {
            throw new BusinessException(CrewErrorCode.TOO_FAR_FROM_LOCATION);
        }

        // 이미 출석했는지 확인
        if (schedule.isCheckedIn(command.memberId())) {
            throw new BusinessException(CrewErrorCode.ALREADY_CHECKED_IN);
        }

        // 출석 처리
        if (!schedule.isParticipating(command.memberId())) {
            throw new BusinessException(CrewErrorCode.NOT_PARTICIPATING_SCHEDULE);
        }
        schedule.checkIn(command.memberId(), now);
        member.updateAttendanceCount();

        // 포인트 적립
        member.addPoints(CHECK_IN_POINTS);

        saveCrewSchedulePort.save(schedule);
        saveMemberPort.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MyAttendanceLogResponse getMyAttendanceLogs(MyAttendanceMonthlyQuery query) {
        LocalDateTime now = LocalDateTime.now();

        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByMemberIdAndMonth(
                query.memberId(), query.yearMonth(), now);

        List<MyAttendanceLogResponse.DailyAttendanceLog> attendanceLogs = crewScheduleResponseMapper.toDailyAttendanceLogs(schedules, query.memberId());

        // 총 출석 날짜 계산
        int attendanceCount = (int) schedules.stream()
                .filter(s -> s.isCheckedIn(query.memberId()))
                .count();

        // 총 적립 포인트 계산
        long totalPoints = attendanceCount * CHECK_IN_POINTS;

        return crewScheduleResponseMapper.toMyAttendanceLogResponse(attendanceCount, totalPoints, attendanceLogs);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MyAttendanceMonthlyListResponse> getMyAttendancesForCalendar(MyAttendanceMonthlyQuery query) {
        LocalDateTime now = LocalDateTime.now();

        Map<LocalDate, Set<RunType>> attendanceMap = loadCrewSchedulePort.findMyMonthlyAttendanceForCalendar(
                query.memberId(), query.yearMonth(), now);

        return crewScheduleResponseMapper.toMyAttendanceMonthlyListResponse(attendanceMap);
    }

    @Transactional
    public void cleanupForExpelledMemberSchedules(Long crewId, Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 해당 멤버가 생성한 미래 일정 조회 (시작 시간 기준)
        List<CrewSchedule> schedulesToCancel = loadCrewSchedulePort.findSchedulesToCancel(crewId, memberId, now);
        // 참여 신청했지만 출석하지 않은 미래 일정 조회 (참여자 기준)
        List<CrewSchedule> schedulesToRemove = loadCrewSchedulePort.findSchedulesToRemoveParticipant(crewId, memberId, now);

        Map<Long, CrewSchedule> scheduleMap = new HashMap<>();
        schedulesToCancel.forEach(s -> scheduleMap.put(s.getId(), s));
        schedulesToRemove.forEach(s -> scheduleMap.put(s.getId(), s));

        scheduleMap.values().forEach(schedule -> {
            // 아직 출석체크하지 않은 일정인 경우 참여 명단에서 제거
            if (!schedule.isCheckedIn(memberId)) schedule.removeParticipant(memberId);


            // 참여 명단에서 본인이 빠진 후 신청자가 0명이 된 경우 일정 취소
            if (schedule.hasNoParticipants()) schedule.cancel();
        });

        saveCrewSchedulePort.saveAll(new ArrayList<>(scheduleMap.values()));
    }


    // 일정이 존재하는지 확인
    private CrewSchedule getSchedule(Long scheduleId) {
        return loadCrewSchedulePort.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 크루 정회원인지 확인
    private CrewMember getJoinedMember(Long crewId, Long memberId) {
        return loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .orElseThrow(() -> {
                    log.warn("[CREW_SCHEDULE] memberId: {} 가 가입하지 않은 crewId: {} 에 접근을 시도했습니다.", memberId, crewId);
                    return new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER);
                });
    }

    // 해당 일정의 생성자인지 확인
    private void validateCreator(CrewSchedule schedule, Long memberId) {
        if (!schedule.getCreatorId().equals(memberId)) {
            log.warn("[CREW_SCHEDULE] memberId: {} 가 본인이 생성하지 않은 scheduleId: {} 에 대해 조작을 시도했습니다.", memberId, schedule.getId());
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }
    }

    // 생성할 일정 유효성 체크
    private void validateSchedule(CrewSchedule schedule) {
        // 현재보다 이후의 시간인지 검증
        if (!schedule.isMeetingTimeValid()) throw new BusinessException(CrewErrorCode.INVALID_SCHEDULE_TIME);
    }

    // 신청하려는 일정과 기존 일정 사이의 간격 검증, 신청하기/일정 생성 시 사용
    private void validateScheduleInterval(Long memberId, LocalDateTime newMeetingAt) {
        validateScheduleInterval(memberId, newMeetingAt, null);
    }

    // 신청하려는 일정과 기존 일정 사이의 간격 검증, 일정 수정 시 사용 (수정중인 일정 제외)
    private void validateScheduleInterval(Long memberId, LocalDateTime newMeetingAt, Long excludeScheduleId) {
        // 사용자가 현재 참여 중인 일정 목록 중 전후 1시간 내에 겹치는 일정 조회
        boolean hasConflict = loadCrewSchedulePort.existsConflictingSchedule(memberId, newMeetingAt, excludeScheduleId);

        if (hasConflict) {
            throw new BusinessException(CrewErrorCode.SCHEDULE_INTERVAL_TOO_CLOSE);
        }
    }
}
