package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import org.springframework.stereotype.Component;

@Component
public class CrewMapper {

    public Crew toDomain(CrewEntity entity) {
        if (entity == null) return null;

        return Crew.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .region(entity.getRegion())
                .invitationCode(entity.getInvitationCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CrewEntity toEntity(Crew crew) {
        return CrewEntity.builder()
                .id(crew.getId())
                .name(crew.getName())
                .category(crew.getCategory())
                .region(crew.getRegion())
                .invitationCode(crew.getInvitationCode())
                .build();
    }
}
