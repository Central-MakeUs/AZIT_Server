package com.youthexpedition.azit.modules.member.application.port.in.dto;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyCrewResponse(
        @Schema(description = "크루 ID")
        Long crewId,
        @Schema(description = "크루 이름")
        String crewName,
        @Schema(description = "크루 이미지 URL")
        String crewImageUrl,
        @Schema(description = "크루 내 역할")
        CrewMemberRole memberRole,
        @Schema(description = "크루 가입 상태")
        CrewMemberStatus memberStatus,
        @Schema(description = "크루 초대 코드")
        String invitationCode
) {
    public static MyCrewResponse of(Long crewId, String crewName, String crewImageUrl,
                                    CrewMemberRole memberRole, CrewMemberStatus memberStatus,
                                    String invitationCode) {
        return new MyCrewResponse(crewId, crewName, crewImageUrl, memberRole, memberStatus, invitationCode);
    }
}
