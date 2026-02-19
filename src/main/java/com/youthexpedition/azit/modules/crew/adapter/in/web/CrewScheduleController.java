package com.youthexpedition.azit.modules.crew.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.docs.CrewScheduleControllerDocs;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateScheduleRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.UpdateScheduleRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CancelScheduleCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

}
