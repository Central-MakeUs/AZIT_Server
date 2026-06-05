package com.youthexpedition.azit.modules.crew.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateScheduleRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.UpdateScheduleRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleMonthlyListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.ParticipantResponse;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Crew Schedule" , description = "크루 일정 API")
public interface CrewScheduleControllerDocs {

    @Operation(
            summary = "크루 일정 생성",
            description = """
            크루 내에서 진행되는 새로운 런(정기런 또는 번개런) 일정을 생성합니다. <br><br>
            
            **[입력 데이터]** <br>
            * 런 종류(runType): REGULAR(정기런), LIGHTNING(번개런) <br>
            * 준비물(supplies): 문자열 리스트 형식으로 전달 <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED)만 일정을 생성할 수 있습니다. (NOT_A_CREW_MEMBER)
            * 정기런은 크루의 리더만 생성 가능합니다. (ONLY_LEADER_CAN_CREATE_REGULAR_RUN)
            * 이미 신청한 일정과 현재 신청하는 일정 간의 시간 차가 60분 미만일 경우 생성이 불가합니다. (SCHEDULE_INTERVAL_TOO_CLOSE)
            * 일정 제목: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 세부 장소: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 준비물(선택): 최대 5개까지 등록 가능하며, 각 항목은 15자 이내여야 합니다. (INVALID_INPUT_VALUE)
            * 모임 시간: 현재 시간보다 과거의 시간으로 생성할 수 없습니다. (INVALID_SCHEDULE_TIME)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_A_CREW_MEMBER", "ONLY_LEADER_CAN_CREATE_REGULAR_RUN", "INVALID_SCHEDULE_TIME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> createSchedule(
            @PathVariable Long crewId, @Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody CreateScheduleRequest request);

    @Operation(
            summary = "크루 일정 수정",
            description = """
            기존에 생성된 크루 일정 정보를 수정합니다. <br>
            준비물 리스트의 경우, 기존 리스트를 모두 대체하는 방식으로 업데이트됩니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 일정을 생성한 본인만 수정할 수 있습니다. (FORBIDDEN_ERROR)
            * 해당 크루의 정회원(JOINED)이어야 합니다. (NOT_A_CREW_MEMBER)
            * 정기런으로 수정하거나 정기런을 수정할 경우, 반드시 리더 권한이 있어야 합니다. (ONLY_LEADER_CAN_CREATE_REGULAR_RUN)
            * 존재하지 않는 일정 ID를 입력할 경우 수정이 불가합니다. (SCHEDULE_NOT_FOUND)
            * 이미 신청한 일정과 현재 신청하는 일정 간의 시간 차가 60분 미만일 경우 수정이 불가합니다. (SCHEDULE_INTERVAL_TOO_CLOSE)
            * 일정 제목: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 세부 장소: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 준비물(선택): 최대 5개까지 등록 가능하며, 각 항목은 15자 이내여야 합니다. (INVALID_INPUT_VALUE)
            * 모임 시간: 현재 시간보다 과거의 시간으로 수정할 수 없습니다. (INVALID_SCHEDULE_TIME)
            * 출석을 시작한 일정은 수정이 불가능합니다. (SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME) <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "NOT_A_CREW_MEMBER", "ONLY_LEADER_CAN_CREATE_REGULAR_RUN", "INVALID_SCHEDULE_TIME", "SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> updateSchedule(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody UpdateScheduleRequest request);

    @Operation(
            summary = "크루 일정 취소(삭제)",
            description = """
            생성된 일정을 취소 상태(CANCELLED)로 변경합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 일정을 생성한 본인만 취소할 수 있습니다. (FORBIDDEN_ERROR)
            * 해당 크루의 정회원(JOINED 상태)이어야 합니다. (NOT_A_CREW_MEMBER)
            * 이미 취소된 일정은 다시 취소할 수 없습니다. (ALREADY_CANCELLED_SCHEDULE)
            * 출석을 시작한 일정은 삭제가 불가능합니다. (SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME)
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "NOT_A_CREW_MEMBER", "ALREADY_CANCELLED_SCHEDULE", "SCHEDULE_MODIFICATION_NOT_ALLOWED_TIME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> cancelSchedule(@PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "크루 일정 참여 신청",
            description = """
            특정 일정에 참여 신청을 합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED 상태)만 신청할 수 있습니다. (NOT_A_CREW_MEMBER)
            * 이미 취소된 일정에는 신청할 수 없습니다. (ALREADY_CANCELLED_SCHEDULE)
            * 이미 신청한 일정에 중복 신청은 불가합니다. (ALREADY_PARTICIPATED)
            * 모집 인원이 마감된(정원 초과) 일정에는 신청할 수 없습니다. (EXCEEDED_MAX_PARTICIPANTS)
            * 이미 신청한 일정과 현재 신청하는 일정 간의 시간 차가 60분 미만일 경우 신청이 불가합니다. (SCHEDULE_INTERVAL_TOO_CLOSE)
            * 출석 가능한 시간이 지난 일정은 참여 신청 및 취소가 불가능합니다.(PARTICIPATION_AND_CANCEL_CLOSED)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_A_CREW_MEMBER", "ALREADY_CANCELLED_SCHEDULE", "ALREADY_PARTICIPATED", "EXCEEDED_MAX_PARTICIPANTS", "SCHEDULE_NOT_FOUND", "SCHEDULE_INTERVAL_TOO_CLOSE", "PARTICIPATION_AND_CANCEL_CLOSED",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> participateSchedule(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "크루 일정 참여 취소",
            description = """
            참여 신청했던 일정에서 참여를 취소합니다. <br><br>
        
            **[참고 사항]** <br>
            * 일정 생성자는 본인의 일정 참여를 취소할 수 없습니다. (CREATOR_CANNOT_CANCEL_PARTICIPATION)
            * 참여하지 않은 일정은 취소할 수 없습니다. (NOT_PARTICIPATING_SCHEDULE)
            * 이미 출석한 일정은 취소할 수 없습니다. (CANNOT_CANCEL_AFTER_CHECK_IN)
            * 출석 가능한 시간이 지난 일정은 참여 신청 및 취소가 불가능합니다.(PARTICIPATION_AND_CANCEL_CLOSED)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_PARTICIPATING_SCHEDULE", "CREATOR_CANNOT_CANCEL_PARTICIPATION", "SCHEDULE_NOT_FOUND", "NOT_A_CREW_MEMBER", "PARTICIPATION_AND_CANCEL_CLOSED",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> cancelParticipation(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "크루 일정 상세 조회",
            description = """
            특정 크루 일정의 상세 정보를 조회합니다. <br><br>
            
            **[참여자 미리보기 목록]** <br>
            * 참여자 목록은 최대 10명까지만 반환됩니다. (미리보기 용도) <br>
            * 정렬 기준: 일정 생성자(0순위) -> 크루 리더(1순위) -> 일반 멤버(신청 시간 순) <br>
            * hasMoreParticipants 값이 true인 경우, 전체 참여자 명단 조회 API를 통해 추가 목록을 확인해야 합니다. <br><br>
            
            **[참고 사항]** <br>
            * 이미 취소(삭제)된 일정은 상세 조회가 불가능합니다. (ALREADY_CANCELLED_SCHEDULE)
            * 존재하지 않는 일정일 경우 예외가 발생합니다. (SCHEDULE_NOT_FOUND)
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "ALREADY_CANCELLED_SCHEDULE", "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CrewScheduleDetailResponse> getScheduleDetail(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "크루 일정 참여자 명단 조회 (무한스크롤)",
            description = """
            특정 일정에 참여 중인 전체 멤버 명단을 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED)만 조회 가능합니다. (NOT_A_CREW_MEMBER)
            * 이미 취소(삭제)된 일정은 명단 조회가 불가능합니다. (ALREADY_CANCELLED_SCHEDULE)
            * 탈퇴하거나 가입 정보가 유실된 회원은 명단에서 제외되어 반환됩니다.
            * 무한 스크롤 방식: hasNext를 통해 다음 페이지 존재 여부를 확인하고, lastId를 다음 요청의 cursorId로 호출하면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "ALREADY_CANCELLED_SCHEDULE", "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<SliceResponse<ParticipantResponse>> getScheduleParticipants(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @Parameter(hidden = true) @CurrentMemberId Long memberId, CursorPageQuery query);

    @Operation(
            summary = "크루 일정 목록 조회",
            description = """
            특정 크루의 일정 목록을 날짜와 러닝 타입별로 필터링하여 조회합니다. <br>

            **[쿼리 파라미터]** <br>
            * date (선택): 특정 날짜(yyyy-MM-dd)의 일정만 조회합니다. 다른 파라미터보다 우선 적용됩니다.
            * startDate / endDate (선택): 조회할 날짜 범위(yyyy-MM-dd)입니다. 두 값이 모두 있어야 동작하며, 주 단위 조회에 활용합니다.
            * yearMonth (선택): 조회할 연월(yyyy-MM)입니다. 미입력 시 현재 월을 기준으로 조회합니다.
            * runType (선택): REGULAR 또는 LIGHTNING으로 필터링합니다. 미입력 시 모든 타입을 조회합니다. <br><br>

            **[파라미터 우선순위]** <br>
            date > startDate·endDate > yearMonth > 현재 월 <br><br>

            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED)만 조회가 가능합니다. (NOT_A_CREW_MEMBER)
            * 결과 목록은 모임 시간(meetingAt)이 빠른 순서대로 정렬되어 반환됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<CrewScheduleListResponse>> getCrewSchedules(
            @PathVariable Long crewId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)") LocalDate date,
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd), endDate와 함께 사용") LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd), startDate와 함께 사용") LocalDate endDate,
            @Parameter(description = "조회 연월 (yyyy-MM)") YearMonth yearMonth,
            @Parameter(description = "러닝 타입") RunType runType,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "월간 크루 일정 목록 조회 (캘린더 표시용)",
            description = """
            특정 월의 날짜별 일정 존재 여부(정기런/번개런)를 조회합니다. 일정이 하나라도 존재하는 날짜만 조회됩니다. <br>
            캘린더에서 각 날짜 하단에 상태 점을 표시하는 데 사용됩니다. <br><br>
            
            **[쿼리 파라미터]** <br>
            * yearMonth (선택): 조회할 연월(yyyy-MM)입니다. 미입력 시 현재 시간 기준의 월을 조회합니다.<br>
            
            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED)만 조회가 가능합니다. (NOT_A_CREW_MEMBER)
            * 취소(삭제)된 일정은 응답에서 제외됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<CrewScheduleMonthlyListResponse>> getMonthlySchedulesForCalendar(
            @PathVariable Long crewId,
            @Parameter(description = "조회 연월 (yyyy-MM)") YearMonth yearMonth,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);
}
