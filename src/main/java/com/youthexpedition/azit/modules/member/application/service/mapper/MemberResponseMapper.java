package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public MyInfoResponse toMyPageResponse(Member member, CrewMember crewMember, Crew crew) {
        return MyInfoResponse.of(
                member.getId(),
                member.getNickname(),
                crewMember.getCrewId(),
                crew.getName(),
                crew.getInvitationCode(),
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl()),
                crewMember.getRole(),
                imageUrlFormatUtil.buildFullImageUrl(member.getProfileImageUrl()),
                member.getTotalAttendanceCount(),
                member.getTotalPoints()
        );
    }
}