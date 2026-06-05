package com.youthexpedition.azit.modules.crew.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.docs.CrewScheduleControllerDocs;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateScheduleRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.UpdateScheduleRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CrewScheduleCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleMonthlyListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.ParticipantResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleMonthlyQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleQuery;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crews/{crewId}/schedules")
@RequiredArgsConstructor
public class CrewScheduleController implements CrewScheduleControllerDocs {
    private final CrewScheduleUseCase crewScheduleUseCase;

    @PostMapping
    public CommonResponse<Void> createSchedule(@PathVariable Long crewId, @CurrentMemberId Long memberId, @Valid @RequestBody CreateScheduleRequest request) {
        crewScheduleUseCase.createSchedule(request.toCommand(crewId, memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PutMapping("/{scheduleId}")
    public CommonResponse<Void> updateSchedule(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId, @Valid @RequestBody UpdateScheduleRequest request) {
        crewScheduleUseCase.updateSchedule(request.toCommand(scheduleId, crewId, memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{scheduleId}")
    public CommonResponse<Void> cancelSchedule(@PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId) {
        CancelScheduleCommand command = CancelScheduleCommand.of(crewId, scheduleId, memberId);
        crewScheduleUseCase.cancelSchedule(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/{scheduleId}/participate")
    public CommonResponse<Void> participateSchedule(@PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId) {
        CrewScheduleCommand command = CrewScheduleCommand.of(crewId, scheduleId, memberId);
        crewScheduleUseCase.participateSchedule(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{scheduleId}/participate")
    public CommonResponse<Void> cancelParticipation(@PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId) {
        CrewScheduleCommand command = CrewScheduleCommand.of(crewId, scheduleId, memberId);
        crewScheduleUseCase.cancelParticipation(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/{scheduleId}")
    public CommonResponse<CrewScheduleDetailResponse> getScheduleDetail(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId) {
        CrewScheduleCommand command = CrewScheduleCommand.of(crewId, scheduleId, memberId);
        CrewScheduleDetailResponse response = crewScheduleUseCase.getScheduleDetail(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/{scheduleId}/participants")
    public CommonResponse<SliceResponse<ParticipantResponse>> getScheduleParticipants(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId, CursorPageQuery query) {
        CrewScheduleCommand command = CrewScheduleCommand.of(crewId, scheduleId, memberId);
        SliceResponse<ParticipantResponse> response = crewScheduleUseCase.getScheduleParticipants(command, query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping
    public CommonResponse<List<CrewScheduleListResponse>> getCrewSchedules(
            @PathVariable Long crewId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) RunType runType,
            @CurrentMemberId Long memberId) {
        CrewScheduleQuery query = CrewScheduleQuery.of(crewId, date, startDate, endDate, yearMonth, runType, memberId);
        List<CrewScheduleListResponse> response = crewScheduleUseCase.getSchedules(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/calendar")
    public CommonResponse<List<CrewScheduleMonthlyListResponse>> getMonthlySchedulesForCalendar(
            @PathVariable Long crewId, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth, @CurrentMemberId Long memberId) {
        CrewScheduleMonthlyQuery query = CrewScheduleMonthlyQuery.of(crewId, yearMonth, memberId);
        List<CrewScheduleMonthlyListResponse> response = crewScheduleUseCase.getMonthlySchedulesForCalendar(query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

}
