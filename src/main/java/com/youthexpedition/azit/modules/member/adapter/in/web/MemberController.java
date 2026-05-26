package com.youthexpedition.azit.modules.member.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentAccessToken;
import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CheckInRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CheckInCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CheckInStatusResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.member.adapter.in.web.docs.MemberControllerDocs;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.AgreeToTermsRequest;
import com.youthexpedition.azit.modules.member.adapter.in.web.dto.UpdateMemberProfileRequest;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.dto.LinkedProviderResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceLogResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyAttendanceMonthlyListResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyCrewResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.application.port.query.MyAttendanceMonthlyQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {
    private final MemberUseCase memberUseCase;
    private final CrewScheduleUseCase crewScheduleUseCase;

    @PostMapping("/terms")
    public CommonResponse<Void> agreeToTerms(@CurrentMemberId Long memberId, @Valid @RequestBody AgreeToTermsRequest request) {
        memberUseCase.agreeToTerms(memberId, request.toCommand());

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/me/withdraw")
    public CommonResponse<Void> withdraw(@CurrentMemberId Long memberId, @CurrentAccessToken String accessToken) {
        memberUseCase.withdraw(memberId, accessToken);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/me")
    public CommonResponse<MyInfoResponse> getMyInfo(@CurrentMemberId Long memberId) {
        MyInfoResponse response = memberUseCase.getMyInfo(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/me/crews")
    public CommonResponse<List<MyCrewResponse>> getMyCrews(@CurrentMemberId Long memberId) {
        List<MyCrewResponse> response = memberUseCase.getMyCrews(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/me/schedules")
    public CommonResponse<List<CrewScheduleListResponse>> getMySchedules(@CurrentMemberId Long memberId) {
        List<CrewScheduleListResponse> response = crewScheduleUseCase.getMySchedules(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/me/check-in-status")
    public CommonResponse<CheckInStatusResponse> getCheckInStatus(@CurrentMemberId Long memberId) {
        CheckInStatusResponse response = crewScheduleUseCase.getCheckInStatus(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PostMapping("/me/schedules/{scheduleId}/check-in")
    public CommonResponse<Void> checkInSchedule(@CurrentMemberId Long memberId, @PathVariable Long scheduleId, @Valid @RequestBody CheckInRequest request) {
        CheckInCommand command = request.toCommand(memberId, scheduleId);
        crewScheduleUseCase.checkInSchedule(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/me/attendances")
    public CommonResponse<MyAttendanceLogResponse> getMyAttendanceLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long crewId,
            @CurrentMemberId Long memberId) {
        MyAttendanceMonthlyQuery query = MyAttendanceMonthlyQuery.of(yearMonth, memberId, crewId);
        MyAttendanceLogResponse response = crewScheduleUseCase.getMyAttendanceLogs(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/me/attendances/calendar")
    public CommonResponse<List<MyAttendanceMonthlyListResponse>> getMyAttendancesForCalendar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long crewId,
            @CurrentMemberId Long memberId) {
        MyAttendanceMonthlyQuery query = MyAttendanceMonthlyQuery.of(yearMonth, memberId, crewId);
        List<MyAttendanceMonthlyListResponse> response = crewScheduleUseCase.getMyAttendancesForCalendar(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/me/providers")
    public CommonResponse<LinkedProviderResponse> getLinkedProviders(@CurrentMemberId Long memberId) {
        LinkedProviderResponse response = memberUseCase.getLinkedProviders(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PatchMapping("/me/profile")
    public CommonResponse<Void> updateMemberProfile(@CurrentMemberId Long memberId, @Valid @RequestBody UpdateMemberProfileRequest request) {
        memberUseCase.updateMemberProfile(memberId, request.toCommand());

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/me/reset-to-pending-terms")
    public CommonResponse<Void> resetToPendingTerms(@CurrentMemberId Long memberId) {
        memberUseCase.resetToPendingTerms(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
