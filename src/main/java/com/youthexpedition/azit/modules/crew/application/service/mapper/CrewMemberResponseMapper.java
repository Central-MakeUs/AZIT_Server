package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrewMemberResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public JoinRequestMemberResponse toResponse(JoinRequestDto joinRequestDto) {
        return new JoinRequestMemberResponse(
                joinRequestDto.memberId(),
                joinRequestDto.nickname(),
                buildFullImageUrl(joinRequestDto.profileImageUrl()),
                joinRequestDto.requestedAt()
        );
    }

    public CrewMemberListResponse toCrewMemberListResponse(long totalCount, SliceResponse<CrewMemberInfoDto> slice) {
        List<CrewMemberDetailResponse> content = slice.content().stream()
                .map(this::toDetailResponse)
                .toList();

        return CrewMemberListResponse.of(
                totalCount,
                content,
                slice.hasNext(),
                slice.lastId()
        );
    }

    public CrewMemberDetailResponse toDetailResponse(CrewMemberInfoDto crewMemberInfoDto) {
        return new CrewMemberDetailResponse(
                crewMemberInfoDto.id(),
                crewMemberInfoDto.memberId(),
                crewMemberInfoDto.nickname(),
                buildFullImageUrl(crewMemberInfoDto.profileImageUrl()),
                crewMemberInfoDto.role(),
                crewMemberInfoDto.joinedAt()
        );
    }

    private String buildFullImageUrl(String imagePath) {
        if (imagePath == null) return null;
        return cloudFrontDomain + imagePath;
    }
}
