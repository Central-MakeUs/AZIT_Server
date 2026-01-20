package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import org.springframework.stereotype.Component;

@Component
public class CrewMemberMapper {

    public CrewMember toDomain(CrewMemberEntity entity) {
        if (entity == null) return null;

        return CrewMember.builder()
                .id(entity.getId())
                .crewId(entity.getCrewId())
                .memberId(entity.getMemberId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .build();
    }

    public CrewMemberEntity toEntity(CrewMember crewMember) {
        return CrewMemberEntity.builder()
                .id(crewMember.getId())
                .crewId(crewMember.getCrewId())
                .memberId(crewMember.getMemberId())
                .role(crewMember.getRole())
                .status(crewMember.getStatus())
                .build();
    }
}
