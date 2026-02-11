package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.model.JoinRequestDto;
import org.springframework.stereotype.Component;

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
}
