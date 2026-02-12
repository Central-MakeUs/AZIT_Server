package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;

public interface SaveCrewMemberPort {
    CrewMember save(CrewMember crewMember);
    void updateAllStatusByMemberId(Long memberId, CrewMemberStatus status);
}
