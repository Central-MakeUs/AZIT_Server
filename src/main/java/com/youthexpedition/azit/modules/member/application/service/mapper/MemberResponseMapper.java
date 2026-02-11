package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MemberResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public MyInfoResponse toMyPageResponse(Member member, CrewMember crewMember) {
        return MyInfoResponse.of(
                member.getId(),
                member.getNickname(),
                crewMember.getCrewId(),
                crewMember.getRole(),
                buildFullImageUrl(member.getProfileImageUrl()),
                member.getTotalAttendanceCount(),
                member.getTotalPoints()
        );
    }

    private String buildFullImageUrl(String imagePath) {
        if (imagePath == null) return null;
        return cloudFrontDomain + imagePath;
    }
}