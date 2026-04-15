package com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import org.springframework.stereotype.Component;

@Component
public class CrewMemberMapper {

    public CrewMember toDomain(CrewMemberEntity entity) {
        if (entity == null) return null;

        return CrewMember.builder()
                .id(entity.getId())
                .crewId(entity.getCrew().getId())
                .memberId(entity.getMemberId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .expelledAt(entity.getExpelledAt())
                .exitedAt(entity.getExitedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CrewMemberEntity toEntity(CrewMember domain) {
        return CrewMemberEntity.builder()
                .id(domain.getId())
                .crew(CrewEntity.builder().id(domain.getCrewId()).build())
                .memberId(domain.getMemberId())
                .role(domain.getRole())
                .status(domain.getStatus())
                .expelledAt(domain.getExpelledAt())
                .exitedAt(domain.getExitedAt())
                .build();
    }
}
