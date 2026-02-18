package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;

import java.util.List;

public interface SaveCrewMemberPort {
    CrewMember save(CrewMember crewMember);
    void saveAll(List<CrewMember> crewMembers);
}
