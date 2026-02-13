package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlProvider;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberResponseMapper {

    private final ImageUrlProvider imageUrlProvider;

    public MyInfoResponse toMyPageResponse(Member member, CrewMember crewMember) {
        return MyInfoResponse.of(
                member.getId(),
                member.getNickname(),
                crewMember.getCrewId(),
                crewMember.getRole(),
                imageUrlProvider.buildFullImageUrl(member.getProfileImageUrl()),
                member.getTotalAttendanceCount(),
                member.getTotalPoints()
        );
    }
}