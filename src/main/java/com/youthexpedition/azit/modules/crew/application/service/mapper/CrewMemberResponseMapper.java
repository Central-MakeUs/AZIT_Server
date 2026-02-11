package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberDetailResponse;
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
                crewMemberInfoDto.nickname(),
                crewMemberInfoDto.profileImageUrl(),
                crewMemberInfoDto.role(),
                crewMemberInfoDto.joinedAt()
        );
    }
}
