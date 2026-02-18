package com.youthexpedition.azit.modules.crew.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "CrewSchedule" , description = "크루 일정 API")
public interface CrewScheduleControllerDocs {

    @Operation(
            summary = "크루 일정 생성",
            description = """
            
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN" // 인증 관련 에러
    })
    CommonResponse<Void> createSchedule(
            @PathVariable Long crewId, @CurrentMemberId Long memberId, @Valid @RequestBody CreateScheduleRequest request);
}
