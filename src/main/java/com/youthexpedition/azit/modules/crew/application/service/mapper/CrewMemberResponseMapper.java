package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrewMemberResponseMapper {

    public JoinRequestMemberResponse toResponse(JoinRequestDto result) {
        return new JoinRequestMemberResponse(
                result.memberId(),
                result.nickname(),
                result.profileImageUrl(),
                result.requestedAt()
        );
    }

    public CrewMemberListResponse toCrewMemberListResponse(List<CrewMemberInfoDto> memberInfos) {
        List<CrewMemberListResponse.CrewMemberDetailResponse> details = memberInfos.stream()
                .map(this::toDetailResponse)
                .toList();

        return CrewMemberListResponse.of(details);
    }

    public CrewMemberListResponse.CrewMemberDetailResponse toDetailResponse(CrewMemberInfoDto dto) {
        return new CrewMemberListResponse.CrewMemberDetailResponse(
                dto.id(),
                dto.nickname(),
                dto.profileImageUrl(),
                dto.role(),
                dto.joinedAt()
        );
    }
}
