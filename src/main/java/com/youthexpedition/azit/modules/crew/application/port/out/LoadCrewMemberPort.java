package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadCrewMemberPort {
    Optional<CrewMember> findByCrewIdAndMemberId(Long crewId, Long memberId);
    Optional<CrewMemberStatus> findStatusByCrewIdAndMemberId(Long crewId, Long memberId);
    List<JoinRequestDto> findJoinRequestsByCrewId(Long crewId);
    Optional<CrewMember> findRecentJoinedCrewMember(Long memberId);
    SliceResponse<CrewMemberInfoDto> findAllJoinedMembersByCrewId(Long crewId, CursorPageQuery query);
    long countJoinedCrewsByMemberId(Long memberId);
    List<CrewMember> findAllByMemberId(Long memberId);
    Map<Long, CrewMember> findAllByCrewIdAndMemberIds(Long crewId, List<Long> memberIds);
}
