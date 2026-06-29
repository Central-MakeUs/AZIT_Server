package com.youthexpedition.azit.modules.crew.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.docs.CrewControllerDocs;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.JoinCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.UpdateCrewProfileRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{crewId}/join-requests")
    public CommonResponse<List<JoinRequestMemberResponse>> getJoinRequests(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        List<JoinRequestMemberResponse> responses = crewUseCase.getJoinRequests(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, responses);
    }

    @GetMapping("/{crewId}/members")
    public CommonResponse<CrewMemberListResponse> getCrewMembers(@PathVariable Long crewId, @CurrentMemberId Long memberId, CursorPageQuery query) {
        CrewMemberListResponse response = crewUseCase.getCrewMembers(crewId, memberId, query);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @DeleteMapping("/{crewId}/join-request")
    public CommonResponse<Void> cancelJoinRequest(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        crewUseCase.cancelJoinRequest(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{crewId}/members/me")
    public CommonResponse<Void> exitCrew(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        crewUseCase.exitCrew(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{crewId}/members/{targetMemberId}")
    public CommonResponse<Void> deleteCrewMember(@PathVariable Long crewId, @PathVariable Long targetMemberId, @CurrentMemberId Long leaderId) {
        crewUseCase.expelCrewMember(crewId, leaderId, targetMemberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @PostMapping("/{crewId}/invitation-code")
    public CommonResponse<InvitationCodeResponse> regenerateInvitationCode(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        InvitationCodeResponse response = crewUseCase.regenerateInvitationCode(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @GetMapping("/{crewId}/info")
    public CommonResponse<CrewInfoResponse> getCrewInfo(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        CrewInfoResponse response = crewUseCase.getCrewInfo(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }

    @PatchMapping("/{crewId}/info")
    public CommonResponse<Void> updateCrewProfile(@PathVariable Long crewId, @CurrentMemberId Long memberId, @Valid @RequestBody UpdateCrewProfileRequest request) {
        crewUseCase.updateCrewProfile(crewId, memberId, request.toCommand());

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @DeleteMapping("/{crewId}")
    public CommonResponse<Void> dissolveCrew(@PathVariable Long crewId, @CurrentMemberId Long memberId) {
        crewUseCase.dissolveCrew(crewId, memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS);
    }

    @GetMapping("/me")
    public CommonResponse<List<JoinedCrewResponse>> getJoinedCrews(@CurrentMemberId Long memberId) {
        List<JoinedCrewResponse> response = crewUseCase.getJoinedCrews(memberId);

        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
