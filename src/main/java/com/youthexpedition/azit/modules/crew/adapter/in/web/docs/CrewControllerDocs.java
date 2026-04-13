package com.youthexpedition.azit.modules.crew.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.JoinCrewRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Crew" , description = "크루 API")
public interface CrewControllerDocs {

    @Operation(
            summary = "크루 생성",
            description = """
            새로운 크루를 생성하고 고유한 초대 코드를 발급합니다. <br>
            성공 시 영문 대문자와 숫자로 조합된 6자리 초대 코드를 반환합니다. <br><br>
            
            **[입력 데이터]** <br>
            * 카테고리(category): RUNNING
            * 활동 지역(region): SEOUL, GYEONGGI_INCHEON, CHUNGCHEONG_DAEJEON, JEOLLA_GWANGJU, GYEONGBUK_DAEGU, GYEONGNAM_BUSAN, GANGWON, JEJU <br><br>
            
            **[제약 사항]** <br>
            * 크루 이름: 최대 15자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 온보딩 단계(PENDING_ONBOARDING) 또는 정회원(ACTIVE) 상태의 사용자만 요청 가능합니다. (INVALID_MEMBER_STATUS)
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN" // 인증 관련 에러
    })
    CommonResponse<CreateCrewResponse> createCrew(
            @Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody CreateCrewRequest request);

    @Operation(
            summary = "크루 가입 신청",
            description = """
            6자리 초대 코드를 입력하여 특정 크루에 가입 신청을 보냅니다. <br>
            크루 리더의 승인이 완료되기 전까지는 정회원(ACTIVE)으로 전환되지 않습니다. <br><br>
            
            **[제약 사항]** <br>
            * 본인이 리더인 크루이거나 이미 멤버로 등록된 크루에는 재신청이 불가합니다. (ALREADY_JOINED_CREW)
            * 유효하지 않은 초대 코드를 입력할 경우 가입이 불가합니다. (CREW_NOT_FOUND)
            """
    )
    @ApiErrorCodeExamples({
            "CREW_NOT_FOUND", "ALREADY_JOINED_CREW",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> joinCrew(@Parameter(hidden = true) @CurrentMemberId Long memberId, @Valid @RequestBody JoinCrewRequest request);


    @Operation(
            summary = "초대 코드로 크루 정보 조회",
            description = """
            입력한 6자리 초대 코드가 유효한지 확인하고, 해당 크루의 요약 정보를 반환합니다. <br>
            사용자가 가입 신청을 하기 전, 크루 정보를 미리 확인할 때 사용합니다. <br><br>
            
            **[제약 사항]** <br>
            * 존재하지 않거나 잘못된 초대 코드일 경우 CREW_NOT_FOUND 오류가 발생합니다.
            """
    )
    @ApiErrorCodeExamples({
            "CREW_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CrewInvitationResponse> getCrewByInvitation(@PathVariable String invitationCode);

    @Operation(
            summary = "가입 요청 상태 조회",
            description = """
            특정 크루에 신청한 멤버(로그인한 사용자) 가입 요청이 어떤 상태인지 조회합니다. <br><br>
            
            **[응답 상태값]** <br>
            * REQUESTED: 승인 대기 중
            * JOINED: 승인 완료
            * REJECTED: 가입 거절
            """
    )
    @ApiErrorCodeExamples({
            "CREW_NOT_FOUND", "NOT_JOINED_CREW",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"})
    CommonResponse<CrewJoinStatusResponse> getCrewJoinStatus(@PathVariable Long crewId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "가입 요청 승인",
            description = """
            크루 리더가 대기 중인 가입 요청을 승인합니다. <br><br>
            
            **[제약 사항]** <br>
            * 해당 크루의 리더(LEADER)만 API를 호출할 수 있습니다. (FORBIDDEN_ERROR)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_CREW_LEADER", "MEMBER_NOT_FOUND", "FORBIDDEN_ERROR", "ALREADY_PROCESSED_JOIN_REQUEST", "JOIN_REQUEST_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"})
    CommonResponse<Void> approveJoinRequest(
            @PathVariable Long crewId, @PathVariable Long targetMemberId, @Parameter(hidden = true) @CurrentMemberId Long leaderId);

    @Operation(
            summary = "가입 요청 거절",
            description = """
            크루 리더가 대기 중인 가입 요청을 거절합니다. <br><br>
            
            **[제약 사항]** <br>
            * 해당 크루의 **리더(LEADER)**만 이 API를 호출할 수 있습니다. (FORBIDDEN_ERROR)
            """
    )
    @ApiErrorCodeExamples({
            "NOT_CREW_LEADER", "FORBIDDEN_ERROR", "ALREADY_PROCESSED_JOIN_REQUEST", "JOIN_REQUEST_NOT_FOUND",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<Void> rejectJoinRequest(
            @PathVariable Long crewId, @PathVariable Long targetMemberId, @Parameter(hidden = true) @CurrentMemberId Long leaderId);

    @Operation(
            summary = "가입 신청 목록 조회",
            description = "크루 리더가 현재 승인 대기 중(REQUESTED)인 유저들의 목록을 조회합니다."
    )
    @ApiErrorCodeExamples({
            "NOT_CREW_LEADER", "FORBIDDEN_ERROR"
    })
    CommonResponse<List<JoinRequestMemberResponse>> getJoinRequests(
            @PathVariable Long crewId, @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "크루 멤버 목록 조회 (무한스크롤)",
            description = """
            커서 기반 페이징을 사용하여 해당 크루에 가입되어 있는 모든 멤버의 목록을 조회합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루에 가입된 멤버(JOINED 상태)만 이 API를 호출할 수 있습니다. (NOT_A_CREW_MEMBER)
            * 리더가 목록 최상단으로 정렬되고, 그 외 멤버는 가입일이 최신인 순서대로 정렬됩니다.
            * 무한 스크롤 방식: hasNext를 통해 다음 페이지 존재 여부를 확인하고, lastId를 다음 요청의 cursorId로 호출하면 됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "CREW_NOT_FOUND", "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<CrewMemberListResponse> getCrewMembers(@PathVariable Long crewId, @CurrentMemberId Long memberId, CursorPageQuery query);

    @Operation(
            summary = "크루 멤버 방출",
            description = """
            크루 리더가 특정 멤버를 크루에서 방출(탈퇴 처리)합니다. <br><br>
            
            **[참고 사항]** <br>
            * 해당 크루의 리더(LEADER)만 이 API를 호출할 수 있습니다. (NOT_CREW_LEADER)
            * 리더 본인은 스스로를 방출할 수 없습니다. (CANNOT_KICK_SELF)
            * 가입 완료(JOINED) 상태인 멤버만 방출 가능합니다. (NOT_A_CREW_MEMBER)
            * 방출 직후 KICKED_PENDING_CONFIRM 상태로 변경됩니다.
            """
    )
    @ApiErrorCodeExamples({
            "NOT_CREW_LEADER", "CREW_NOT_FOUND", "NOT_A_CREW_MEMBER", "CANNOT_KICK_SELF",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN"
    })
    CommonResponse<Void> deleteCrewMember(@PathVariable Long crewId, @PathVariable Long targetMemberId, @Parameter(hidden = true) @CurrentMemberId Long leaderId);

    @Operation(
            summary = "초대 코드 재발급",
            description = """
                    크루 리더가 기존 초대 코드를 폐기하고 새로운 초대 코드를 발급합니다. <br><br>

                    **[제약 사항]** <br>
                    * 해당 크루의 리더(LEADER)만 해당 API를 호출할 수 있습니다. (NOT_CREW_LEADER) <br>
                    * 재발급 즉시 기존 코드는 무효화됩니다.
                    """
    )
    @ApiErrorCodeExamples({
            "CREW_NOT_FOUND", "NOT_CREW_LEADER", "INVITATION_CODE_GENERATION_FAILED", "NOT_A_CREW_MEMBER",
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN"
    })
    CommonResponse<InvitationCodeResponse> regenerateInvitationCode(
            @PathVariable Long crewId, @Parameter(hidden = true) @CurrentMemberId Long memberId);
}
