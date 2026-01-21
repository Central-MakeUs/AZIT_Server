package com.youthexpedition.azit.modules.crew.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.crew.adapter.in.web.docs.CrewControllerDocs;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.JoinCrewRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
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
}
