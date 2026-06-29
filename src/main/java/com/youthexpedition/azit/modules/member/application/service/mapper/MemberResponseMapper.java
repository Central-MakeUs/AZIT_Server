package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.image.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyCrewResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public MyInfoResponse toMyInfoResponse(Member member) {
        return MyInfoResponse.of(
                member.getId(),
                member.getNickname(),
                imageUrlFormatUtil.buildFullImageUrl(member.getProfileImageUrl()),
                member.getTotalPoints()
        );
    }

    public MyCrewResponse toMyCrewResponse(CrewMember crewMember, Crew crew) {
        // JOINED 상태일 때 초대 코드 노출
        String invitationCode = crewMember.getStatus() == CrewMemberStatus.JOINED
                ? crew.getInvitationCode() : null;

        return MyCrewResponse.of(
                crew.getId(),
                crew.getName(),
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl()),
                crewMember.getStatus() == CrewMemberStatus.REQUESTED ? null : crewMember.getRole(),
                crewMember.getStatus(),
                invitationCode
        );
    }
}
