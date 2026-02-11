package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.application.port.out.model.JoinRequestDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;

import java.util.List;
import java.util.Optional;

public interface LoadCrewMemberPort {
    Optional<CrewMember> findByCrewIdAndMemberId(Long crewId, Long memberId);
    long countJoinedMembersByCrewId(Long crewId);
    Optional<CrewMemberStatus> findStatusByCrewIdAndMemberId(Long crewId, Long memberId);
    List<JoinRequestDto> findJoinRequestsByCrewId(Long crewId);
    Optional<CrewMember> findRecentJoinedCrewMember(Long memberId);
}
