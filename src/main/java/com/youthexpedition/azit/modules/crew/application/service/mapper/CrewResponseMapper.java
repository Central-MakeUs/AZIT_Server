package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInvitationResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewJoinStatusResponse;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrewResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;


    public CreateCrewResponse toCreateResponse(Crew crew) {
        return CreateCrewResponse.of(
                crew.getInvitationCode(),
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl())
        );
    }

    public CrewInvitationResponse toInvitationResponse(Crew crew) {
        return CrewInvitationResponse.of(
                crew.getId(),
                crew.getName(),
                crew.getCategory().name(),
                crew.getMemberCount(),
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl()),
                crew.getDescription()
        );
    }

    public CrewJoinStatusResponse toJoinStatusResponse(Crew crew, CrewMemberStatus status) {
        return CrewJoinStatusResponse.of(
                crew.getId(),
                crew.getName(),
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl()),
                status
        );
    }
}
