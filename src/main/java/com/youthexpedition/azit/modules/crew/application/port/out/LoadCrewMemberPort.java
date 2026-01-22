package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;

import java.util.Optional;

public interface LoadCrewMemberPort {
    boolean existsByCrewIdAndMemberId(Long crewId, Long memberId);
    long countJoinedMembersByCrewId(Long crewId);
    Optional<CrewMemberStatus> findStatusByCrewIdAndMemberId(Long crewId, Long memberId);
}
