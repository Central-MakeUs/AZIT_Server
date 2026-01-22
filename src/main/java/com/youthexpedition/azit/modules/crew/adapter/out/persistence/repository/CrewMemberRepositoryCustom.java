package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.application.port.out.model.JoinRequestQueryResult;

import java.util.List;

public interface CrewMemberRepositoryCustom {
    List<JoinRequestQueryResult> findJoinRequestsByCrewId(Long crewId);
}
