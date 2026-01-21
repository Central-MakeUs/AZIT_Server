package com.youthexpedition.azit.modules.crew.application.port.out;

public interface LoadCrewMemberPort {
    boolean existsByCrewIdAndMemberId(Long crewId, Long memberId);
    long countByCrewIdAndStatus(Long crewId);
}
