package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.application.port.in.dto.JoinRequestMemberResponse;

import java.util.List;

public interface CrewMemberRepositoryCustom {
    List<JoinRequestMemberResponse> findJoinRequestsByCrewId(Long crewId);
}
