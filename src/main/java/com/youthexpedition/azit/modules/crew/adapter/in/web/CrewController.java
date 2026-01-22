package com.youthexpedition.azit.modules.crew.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.docs.CrewControllerDocs;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.JoinCrewRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInvitationResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewJoinStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crews")
@RequiredArgsConstructor
public class CrewController implements CrewControllerDocs {
    private final CrewUseCase crewUseCase;

    @PostMapping
    public CommonResponse<CreateCrewResponse> createCrew(@CurrentMemberId Long memberId, @Valid @RequestBody CreateCrewRequest request) {
        CreateCrewResponse response = crewUseCase.createCrew(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PostMapping("/join")
    public CommonResponse<Void> joinCrew(@CurrentMemberId Long memberId, @Valid @RequestBody JoinCrewRequest request) {
        crewUseCase.joinCrew(request.toCommand(memberId));

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/invitation/{invitationCode}")
    public CommonResponse<CrewInvitationResponse> getCrewByInvitation(@PathVariable String invitationCode) {
        CrewInvitationResponse response = crewUseCase.getCrewInfoByInvitationCode(invitationCode);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/{crewId}/join-status")
    public CommonResponse<CrewJoinStatusResponse> getCrewJoinStatus(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        CrewJoinStatusResponse response = crewUseCase.getCrewJoinStatus(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PostMapping("/{crewId}/join-requests/{targetMemberId}/approve")
    public CommonResponse<Void> approveJoinRequest(@PathVariable Long crewId, @PathVariable Long targetMemberId, @CurrentMemberId Long leaderId) {
        ProcessJoinCommand command = ProcessJoinCommand.of(crewId, targetMemberId, leaderId);
        crewUseCase.approveJoinRequest(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/{crewId}/join-requests/{targetMemberId}/reject")
    public CommonResponse<Void> rejectJoinRequest(@PathVariable Long crewId, @PathVariable Long targetMemberId, @CurrentMemberId Long leaderId) {
        ProcessJoinCommand command = ProcessJoinCommand.of(crewId, targetMemberId, leaderId);
        crewUseCase.rejectJoinRequest(command);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }
}
