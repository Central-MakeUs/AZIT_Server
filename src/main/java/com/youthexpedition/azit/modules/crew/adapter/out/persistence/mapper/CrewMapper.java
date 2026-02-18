package com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper;

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
                .imageUrl(entity.getImageUrl())
                .invitationCode(entity.getInvitationCode())
                .memberCount(entity.getMemberCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CrewEntity toEntity(Crew domain) {
        return CrewEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .category(domain.getCategory())
                .region(domain.getRegion())
                .imageUrl(domain.getImageUrl())
                .invitationCode(domain.getInvitationCode())
                .memberCount(domain.getMemberCount())
                .status(domain.getStatus())
                .build();
    }
}
