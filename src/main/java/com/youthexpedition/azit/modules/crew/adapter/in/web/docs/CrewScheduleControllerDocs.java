package com.youthexpedition.azit.modules.crew.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateScheduleRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.UpdateScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Crew Schedule" , description = "크루 일정 API")
public interface CrewScheduleControllerDocs {

    @Operation(
            summary = "크루 일정 생성",
            description = """
            크루 내에서 진행되는 새로운 런(정기런 또는 번개런) 일정을 생성합니다. <br><br>
            
            **[입력 데이터]** <br>
            * 런 종류(runType): REGULAR(정기런), LIGHTNING(번개런) <br>
            * 오전/오후(amPm): 오전, 오후 <br>
            * 준비물(supplies): 문자열 리스트 형식으로 전달 <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루의 정회원(JOINED)만 일정을 생성할 수 있습니다. (NOT_A_CREW_MEMBER)
            * 정기런은 크루의 리더만 생성 가능합니다. (ONLY_LEADER_CAN_CREATE_REGULAR_RUN)
            * 일정 제목: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 세부 장소: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 준비물: 최대 5개까지 등록 가능하며, 각 항목은 15자 이내여야 합니다. (INVALID_INPUT_VALUE)
            * 모임 시간: 현재 시간보다 과거의 시간으로 생성할 수 없습니다. (INVALID_SCHEDULE_TIME)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_A_CREW_MEMBER", "ONLY_LEADER_CAN_CREATE_REGULAR_RUN", "INVALID_SCHEDULE_TIME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> createSchedule(
            @PathVariable Long crewId, @CurrentMemberId Long memberId, @Valid @RequestBody CreateScheduleRequest request);

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
            * 일정 제목: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 세부 장소: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 준비물: 최대 5개까지 등록 가능하며, 각 항목은 15자 이내여야 합니다. (INVALID_INPUT_VALUE)
            * 모임 시간: 현재 시간보다 과거의 시간으로 수정할 수 없습니다. (INVALID_SCHEDULE_TIME) <br><br>
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "NOT_A_CREW_MEMBER", "ONLY_LEADER_CAN_CREATE_REGULAR_RUN", "INVALID_SCHEDULE_TIME",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> updateSchedule(
            @PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId, @Valid @RequestBody UpdateScheduleRequest request);

    @Operation(
            summary = "크루 일정 취소(삭제)",
            description = """
            생성된 일정을 취소 상태(CANCELLED)로 변경합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 일정을 생성한 본인 또는 크루 리더만 취소할 수 있습니다. (FORBIDDEN_ERROR)
            * 해당 크루의 정회원(JOINED 상태)이어야 합니다. (NOT_A_CREW_MEMBER)
            * 이미 취소된 일정은 다시 취소할 수 없습니다. (ALREADY_CANCELLED_SCHEDULE)
            """
    )
    @ApiErrorCodeExamples({
            "SCHEDULE_NOT_FOUND", "NOT_A_CREW_MEMBER", "ALREADY_CANCELLED_SCHEDULE",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN"
    })
    CommonResponse<Void> cancelSchedule(@PathVariable Long crewId, @PathVariable Long scheduleId, @CurrentMemberId Long memberId);
}
