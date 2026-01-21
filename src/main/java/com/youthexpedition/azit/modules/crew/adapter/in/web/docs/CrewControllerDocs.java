package com.youthexpedition.azit.modules.crew.adapter.in.web.docs;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.config.swagger.ApiErrorCodeExamples;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.CreateCrewRequest;
import com.youthexpedition.azit.modules.crew.adapter.in.web.dto.JoinCrewRequest;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

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
            * 크루 이름: 최대 20자 이내로 작성해야 합니다. (INVALID_INPUT_VALUE)
            * 온보딩 단계(PENDING_ONBOARDING) 또는 정회원(ACTIVE) 상태의 사용자만 요청 가능합니다. (INVALID_MEMBER_STATUS)
            """
    )
    @ApiErrorCodeExamples({
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN" // 인증 관련 에러
    })
    CommonResponse<CreateCrewResponse> createCrew(@CurrentMemberId Long memberId, @Valid @RequestBody CreateCrewRequest request);

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
            "UNAUTHORIZED", "EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_REUSE_DETECTED", "BLACKLISTED_TOKEN" // 인증 관련 에러
    })
    CommonResponse<Void> joinCrew(@CurrentMemberId Long memberId, @Valid @RequestBody JoinCrewRequest request);
}
