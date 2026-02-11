package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyPageResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MemberResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public MyPageResponse toMyPageResponse(Member member, CrewMemberRole crewMemberRole) {
        return MyPageResponse.of(
                member.getId(),
                member.getNickname(),
                crewMemberRole,
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