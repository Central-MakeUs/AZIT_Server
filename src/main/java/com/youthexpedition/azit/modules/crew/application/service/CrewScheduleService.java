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

    private static final int CHECK_IN_COOL_DOWN_MINUTES = 30; // 출석 완료 후 최소 유지 시간
    private static final int ACTIVE_CHECK_IN_WINDOW_HOURS = 1; // 출석 버튼 활성화 윈도우 (전후 1시간)
    private static final int COMPLETED_RETENTION_HOURS = 3;    // 지난 일정 완료 표시 유지 시간
    private static final int MINIMUM_SCHEDULE_INTERVAL_MINUTES = 60; // 최소 일정 간격
    private static final long CHECK_IN_POINTS = 100L;
    private static final double CHECK_IN_AVAILABLE_DISTANCE_METERS = 100.0;

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
        saveCrewSchedulePort.save(crewSchedule);
    }

    @Override
    public void updateSchedule(UpdateScheduleCommand command) {
        CrewSchedule schedule = getSchedule(command.scheduleId());

        // 본인이 생성한 일정인지 확인
        validateCreator(schedule, command.creatorId());

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

        // 크루 정회원인지 확인
        getJoinedMember(command.crewId(), command.memberId());

        schedule.removeParticipant(command.memberId());
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

        List<Long> participantIds = schedule.getParticipantIds();
        Map<Long, MemberProfileDto> profileMap = loadMemberPort.findAllByIds(participantIds);
        Map<Long, CrewMember> crewMemberMap = loadCrewMemberPort.findAllByCrewIdAndMemberIds(command.crewId(), participantIds);

        // 데이터 정합성 검증
        if (profileMap.size() != participantIds.size() || crewMemberMap.size() != participantIds.size()) {
            log.warn("Data inconsistency detected for schedule {}: participants={}, profiles={}, crewMembers={}",
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
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByFilter(query.crewId(), query.date(), query.runType());

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, query.memberId());
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

        return crewScheduleResponseMapper.toScheduleListResponse(schedules, memberId);
    }

    @Transactional(readOnly = true)
    @Override
    public CheckInStatusResponse getCheckInStatus(Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 오늘 참여하는 모든 일정 조회
        List<CrewSchedule> todaySchedules = loadCrewSchedulePort.findAllTodaySchedulesByMemberId(memberId, now);
        // 가장 가까운 미래 일정 조회
        Optional<CrewSchedule> nextSchedule = loadCrewSchedulePort.findNextClosestScheduleByMemberId(memberId, now);

        // 30분 이내에 출석을 완료한 경우, 다음 일정이 있더라도 출석 완료 상태를 30분간 노출
        // 출석하기 비활성화
        Optional<CrewSchedule> justCompletedSchedule = todaySchedules.stream()
                .filter(s -> s.isCheckedIn(memberId))
                .filter(s -> s.getMeetingAt().isBefore(now) &&
                        now.isBefore(s.getMeetingAt().plusMinutes(CHECK_IN_COOL_DOWN_MINUTES))) // 30분간 출석 완료 상태 유지
                .findFirst();

        if (justCompletedSchedule.isPresent()) {
            CrewSchedule schedule = justCompletedSchedule.get();
            LocalDateTime checkInTime = schedule.getParticipants().get(memberId).getCheckedInAt();
            return crewScheduleResponseMapper.toTodayScheduleCheckInStatus(schedule, true, checkInTime, false);
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

        // 출석을 완료했고, 일정이 시작한지 3시간 이내인 일정 필터링
        // 출석 완료 활성화, 출석하기 비활성화
        Optional<CrewSchedule> recentlyCompletedSchedule = todaySchedules.stream()
                .filter(s -> s.isCheckedIn(memberId))
                .filter(s -> s.getMeetingAt().isBefore(now) &&
                        s.getMeetingAt().isAfter(now.minusHours(COMPLETED_RETENTION_HOURS)))
                .max(Comparator.comparing(CrewSchedule::getMeetingAt));

        if (recentlyCompletedSchedule.isPresent()) {
            CrewSchedule schedule = recentlyCompletedSchedule.get();
            LocalDateTime checkInTime = schedule.getParticipants().get(memberId).getCheckedInAt();
            return crewScheduleResponseMapper.toTodayScheduleCheckInStatus(recentlyCompletedSchedule.get(), true, checkInTime, false);
        }

        // 오늘 남은 일정 중 가장 빠른 일정 필터링
        // 출석하기 비활성화
        Optional<CrewSchedule> upcomingTodaySchedule = todaySchedules.stream()
                .filter(s -> !s.isCheckedIn(memberId))
                .filter(s -> s.getMeetingAt().isAfter(now))
                .findFirst();

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

        // 거리 검증 (100m 이내)
        double distance = LocationDistanceUtil.calculateDistance(
                schedule.getLocation().getLatitude(), schedule.getLocation().getLongitude(), command.latitude(), command.longitude());
        log.debug("distance: {}", distance);

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

        // 포인트 적립
        member.addPoints(CHECK_IN_POINTS);

        saveCrewSchedulePort.save(schedule);
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void cancelAllParticipationInCrew(Long crewId, Long memberId) {
        // 해당 크루의 일정 중 사용자가 참여 중인 모든 일정 조회
        List<CrewSchedule> joinedSchedules = loadCrewSchedulePort.findAllByCrewIdAndMemberId(crewId, memberId);

        if (joinedSchedules.isEmpty()) return;
        // 참여한 일정 데이터 삭제
        joinedSchedules.forEach(schedule -> schedule.removeParticipant(memberId));

        saveCrewSchedulePort.saveAll(joinedSchedules);
    }

    @Override
    @Transactional(readOnly = true)
    public MyAttendanceLogResponse getMyAttendanceLogs(MyAttendanceMonthlyQuery query) {
        List<CrewSchedule> schedules = loadCrewSchedulePort.findAllByMemberIdAndMonth(
                query.memberId(), query.yearMonth());

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
        Map<LocalDate, Set<RunType>> attendanceMap = loadCrewSchedulePort.findMyMonthlyAttendanceForCalendar(
                query.memberId(), query.yearMonth());

        return crewScheduleResponseMapper.toMyAttendanceMonthlyListResponse(attendanceMap);
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
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));
    }

    // 해당 일정의 생성자인지 확인
    private void validateCreator(CrewSchedule schedule, Long memberId) {
        if (!schedule.getCreatorId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }
    }

    // 생성할 일정 유효성 체크
    private void validateSchedule(CrewSchedule schedule) {
        // 현재보다 이후의 시간인지 검증
        if (!schedule.isMeetingTimeValid()) throw new BusinessException(CrewErrorCode.INVALID_SCHEDULE_TIME);
    }

    // 신청하려는 일정과 기존 일정 사이의 간격 검증
    private void validateScheduleInterval(Long memberId, LocalDateTime newMeetingAt) {
        // 사용자가 현재 참여 중인 일정 목록 조회
        List<CrewSchedule> joinedSchedules = loadCrewSchedulePort.findAllByMemberId(memberId);

        for (CrewSchedule joined : joinedSchedules) {
            // 두 일정 사이의 차이 계산
            long minutesBetween = Math.abs(ChronoUnit.MINUTES.between(joined.getMeetingAt(), newMeetingAt));

            if (minutesBetween < MINIMUM_SCHEDULE_INTERVAL_MINUTES) {
                throw new BusinessException(CrewErrorCode.SCHEDULE_INTERVAL_TOO_CLOSE);
            }
        }
    }
}
