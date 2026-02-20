package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewMemberListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrewMemberResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;


    public JoinRequestMemberResponse toJoinRequestResponse(JoinRequestDto joinRequestDto) {
        return new JoinRequestMemberResponse(
                joinRequestDto.memberId(),
                joinRequestDto.nickname(),
                imageUrlFormatUtil.buildFullImageUrl(joinRequestDto.profileImageUrl()),
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
                imageUrlFormatUtil.buildFullImageUrl(crewMemberInfoDto.profileImageUrl()),
                crewMemberInfoDto.role(),
                crewMemberInfoDto.joinedAt()
        );
    }
}
