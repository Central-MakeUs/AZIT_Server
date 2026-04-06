package com.youthexpedition.azit.modules.member.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CheckInRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CheckInStatusResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AgreeToTermsRequest;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.UpdateNicknameRequest;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceLogResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceMonthlyListResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.List;

@Tag(name = "Member" , description = "회원 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "약관 동의",
            description = """
            소셜 로그인 직후 '약관 동의 대기(PENDING_TERMS)' 상태인 회원이 필수 서비스 약관에 동의하는 단계입니다. <br><br>
            
            **[제약 사항]** <br>
            * '약관 동의 대기(PENDING_TERMS)' 상태의 회원만 호출 가능합니다. (INVALID_MEMBER_STATUS)
            * 필수 약관 중 하나라도 누락될 경우 가입이 진행되지 않습니다. (REQUIRED_TERMS_NOT_AGREED)
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND",
            "REQUIRED_TERMS_NOT_AGREED", // 필수 약관 중 하나라도 동의하지 않은 경우
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> agreeToTerms(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody AgreeToTermsRequest request);

    @Operation(
            summary = "회원 탈퇴",
            description = """
            서비스 이용을 중단하고 회원의 소셜 연동 해제 및 탈퇴 처리를 진행합니다. <br><br>
            
            **[참고 사항]** <br>
            * 리더로 소속된 크루에 다른 멤버가 존재할 경우 탈퇴가 불가능합니다. (CANNOT_WITHDRAW_AS_LEADER)
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_ALREADY_WITHDRAWN", "APPLE_REVOKE_FAILED",  "KAKAO_REVOKE_FAILED", "CANNOT_WITHDRAW_AS_LEADER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> withdraw(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Parameter(hidden = true) @CurrentAccessToken String accessToken);

    @Operation(
            summary = "가입 승인/거절 및 크루 방출 결과 확인",
            description = """
            사용자가 가입 신청 결과(승인/거절) 또는 크루 방출 통보를 확인했음을 서버에 알립니다. <br><br>
            
            **[참고 사항]** <br>
            * APPROVED_PENDING_CONFIRM, REJECTED_PENDING_CONFIRM, KICKED_PENDING_CONFIRM 상태인 회원만 호출 가능합니다. (INVALID_MEMBER_STATUS)
            * 가입 승인 확인 (APPROVED_PENDING_CONFIRM): 즉시 ACTIVE로 변경됩니다. <br>
            * 가입 거절/크루 방출 확인 (REJECTED_PENDING_CONFIRM / KICKED_PENDING_CONFIRM): 가입되어 있는 다른 크루가 1개 이상 있는 경우 ACTIVE 상태가 유지되고, 가입되어 있는 다른 크루가 없는 경우 PENDING_ONBOARDING으로 변경됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND", "INVALID_MEMBER_STATUS",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> confirmMemberStatus(@Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "내 정보 조회 (마이페이지 및 크루 정보 확인용)",
            description = """
            로그인한 사용자의 기본 프로필 정보와 소속(또는 관련) 크루 정보를 조회합니다. <br>
            회원의 현재 상태에 따라 반환되는 크루 정보의 의미가 달라집니다. <br><br>
            
            **[상태별 크루 정보 반환 규칙]** <br>
            1. ACTIVE: 가장 최근에 가입한 크루 정보를 반환합니다. <br>
            2. KICKED_PENDING_CONFIRM: 방출 통보 화면을 띄우기 위해, 방금 방출된 크루 정보를 반환합니다. <br>
            3. REJECTED_PENDING_CONFIRM: 가입 거절 안내 화면을 위해, 가장 최근에 가입이 거절된 크루 정보를 반환합니다. <br>
            4. WAITING_FOR_APPROVE: 승인 대기 화면을 위해, 가장 최근에 가입 신청한 크루 정보를 반환합니다. <br>
            5. PENDING_TERMS, PENDING_ONBOARDING, WITHDRAWN: 크루 관련 정보는 모두 null로 반환됩니다. <br><br>
            
            **[참고 사항]** <br>
            * invitationCode: 사용자가 해당 크루의 리더이면서 JOINED 상태일 때만 값이 존재하며, 그 외의 경우에는 null로 내려갑니다.
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND", "CREW_MEMBER_NOT_FOUND", "CREW_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<MyInfoResponse> getMyInfo(@Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(
            summary = "내 일정 목록 조회",
            description = """
            현재 로그인한 사용자가 참여(신청) 중인 모든 크루의 일정 목록을 조회합니다. <br>
            홈 탭에서 사용자가 앞으로 참여해야 할 일정들을 확인하는 데 사용됩니다. <br><br>
            
            **[참고 사항]** <br>
            * 본인이 참여 신청을 완료한 일정만 반환됩니다.
            * 취소(삭제)된 일정은 응답에서 제외됩니다.
            * 오늘 현재 시간 이후의 일정만 반환됩니다. (지난 일정 제외)
            * 모임 시간이 가장 가까운 순서대로 정렬됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<List<CrewScheduleListResponse>> getMySchedules(@Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "오늘의 러닝 및 출석 현황 조회 (홈 위젯용)",
            description = """
            홈 화면 최상단 위젯에 표시될 사용자의 실시간 러닝 및 출석 상태를 조회합니다. <br>
            오늘 참여할 일정의 활성화 여부와 다음 일정까지의 남은 기간(D-Day) 정보를 포함합니다. <br><br>
            
            **[참고 사항]** <br>
            * 출석 가능 시간 (isAvailableTime): 일정 시작 1시간 전부터 1시간 후 사이인 경우 true를 반환합니다.
            * 일정 시작 시점부터 최소 1시간, 혹은 일정 시작 후 최대 3시간까지 출석 완료 화면을 유지합니다.
            * 하루에 여러 일정이 있을 때, 앞선 일정 시작 이후 1시간 동안은 다음 일정이 활성화되는 시간(1시간 전)이더라도 출석 완료를 유지합니다.
            <br><br>
            
            **[UI 설정 가이드]** <br>
            * 출석하기 활성화: isCheckedIn == false && isAvailableTime == true && (GPS 거리 100m 이내) <br>
            * 출석하기 비활성화: isCheckedIn == false && (isAvailableTime == false || GPS 거리 100m 밖) <br>
            * 출석 완료: isCheckedIn == true <br>
            * D-Day: hasScheduleToday == false 일 경우 daysLeft 필드 활용
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CheckInStatusResponse> getCheckInStatus(@Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "일정 출석",
            description = """
            사용자의 현재 위치와 시간을 검증하여 특정 일정에 대한 출석을 처리합니다. <br><br>
            
            **[참고 사항]** <br>
            * 일정 시작 시간 전후 1시간 이내여야 합니다. (NOT_CHECK_IN_TIME) <br>
            * 사용자의 현재 위치가 일정 집결지로부터 100m 이내여야 합니다. (TOO_FAR_FROM_LOCATION) <br>
            * 해당 일정에 참여 신청이 완료된 회원이어야 합니다. (NOT_PARTICIPATING_SCHEDULE) <br>
            * 이미 출석을 완료한 일정은 다시 처리할 수 없습니다. (ALREADY_CHECKED_IN) <br>
            * 출석 보상으로 100 포인트가 즉시 적립됩니다. <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "MEMBER_NOT_FOUND", "NOT_CHECK_IN_TIME", "TOO_FAR_FROM_LOCATION", "ALREADY_CHECKED_IN", "NOT_PARTICIPATING_SCHEDULE",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> checkInSchedule(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Parameter(description = "일정 ID") Long scheduleId,
            @Valid @RequestBody CheckInRequest request
    );

    @Operation(
            summary = "내 출석 로그 목록 조회",
            description = """
            사용자의 월별 출석 횟수, 누적 획득 포인트 및 상세 활동 이력을 조회합니다. <br>
            
            **[쿼리 파라미터]** <br>
            * yearMonth (선택): 조회할 연월(yyyy-MM)입니다. 미입력 시 현재 시간 기준의 월을 조회합니다.<br>
            
            **[참고 사항]** <br>
            * 아직 모임 시간이 지나지 않았고 출석도 하지 않은 예정 일정은 리스트에 나타나지 않습니다. <br>
            * 모임 시간이 이미 지난 일정(출석/결석 확정) 또는 모임 시간 전이라도 출석을 완료한 일정만 반환됩니다. <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<MyAttendanceLogResponse> getMyAttendanceLogs(
            @Parameter(description = "조회 연월 (yyyy-MM)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(
            summary = "월간 내 출석 로그 목록 조회 (캘린더 표시용)",
            description = """
            특정 월의 날짜별 출석 상태(정기런/번개런)를 조회합니다. 신청한 일정이 하나라도 존재하는 날짜만 조회됩니다. <br>
            캘린더에서 각 날짜 하단에 상태 점을 표시하는 데 사용됩니다. <br><br>
            
            **[쿼리 파라미터]** <br>
            * yearMonth (선택): 조회할 연월(yyyy-MM)입니다. 미입력 시 현재 시간 기준의 월을 조회합니다.<br>
            
            **[참고 사항]** <br>
                * 아직 모임 시간이 지나지 않았고 출석도 하지 않은 예정 일정은 리스트에 나타나지 않습니다. <br>
                * 모임 시간이 이미 지난 일정(출석/결석 확정) 또는 모임 시간 전이라도 출석을 완료한 일정만 반환됩니다. <br><br>
            """
    )
    CommonResponse<List<MyAttendanceMonthlyListResponse>> getMyAttendancesForCalendar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @Parameter(hidden = true) @CurrentMemberId Long memberId

    );

    @Operation(
            summary = "닉네임 수정",
            description = """
            로그인한 사용자의 닉네임을 수정합니다. <br><br>

            **[제약 사항]** <br>
            * 닉네임은 최대 10자까지 입력 가능합니다. (INVALID_NICKNAME) <br>
            * 특수문자는 사용할 수 없으며, 한글/영문/숫자만 허용됩니다. (INVALID_NICKNAME)
            """
    )
    @ApiErrorCodeExamples({
            "MEMBER_NOT_FOUND", "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> updateNickname(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody UpdateNicknameRequest request);


}
