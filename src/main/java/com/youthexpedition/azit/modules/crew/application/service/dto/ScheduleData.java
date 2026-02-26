package com.youthexpedition.azit.modules.crew.application.service.dto;

import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;

import java.util.Map;

public record ScheduleData(
        CrewSchedule schedule,
        Map<Long, MemberProfileDto> profileMap,
        Map<Long, CrewMember> crewMemberMap
) {
}
