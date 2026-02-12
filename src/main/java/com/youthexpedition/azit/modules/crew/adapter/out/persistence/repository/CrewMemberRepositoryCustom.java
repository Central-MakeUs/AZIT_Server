package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;

import java.util.List;

public interface CrewMemberRepositoryCustom {
    List<JoinRequestDto> findJoinRequestsByCrewId(Long crewId);
    SliceResponse<CrewMemberInfoDto> findAllJoinedMembersByCrewId(Long crewId, CursorPageQuery query);
}
