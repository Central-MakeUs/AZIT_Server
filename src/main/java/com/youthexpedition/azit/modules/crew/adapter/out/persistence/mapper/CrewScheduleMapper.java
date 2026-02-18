package com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleSupplyEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.LocationEntity;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.Location;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CrewScheduleMapper {

    public CrewSchedule toDomain(CrewScheduleEntity entity) {
        if (entity == null) return null;

        return CrewSchedule.builder()
                .id(entity.getId())
                .crewId(entity.getCrewId())
                .creatorId(entity.getCreatorId())
                .title(entity.getTitle())
                .runType(entity.getRunType())
                .meetingAt(entity.getMeetingAt())
                .location(Location.builder()
                        .name(entity.getLocationEntity().getName())
                        .address(entity.getLocationEntity().getAddress())
                        .detailedLocation(entity.getLocationEntity().getDetailedLocation())
                        .latitude(entity.getLocationEntity().getLatitude())
                        .longitude(entity.getLocationEntity().getLongitude())
                        .build())
                .description(entity.getDescription())
                .distance(entity.getDistance())
                .pace(entity.getPace())
                .maxParticipants(entity.getMaxParticipants())
                .supplies(entity.getSupplies().stream()
                        .map(CrewScheduleSupplyEntity::getContent)
                        .toList())
                .status(entity.getStatus())
                .build();
    }

    public CrewScheduleEntity toEntity(CrewSchedule domain) {
        if (domain == null) return null;

        return CrewScheduleEntity.builder()
                .id(domain.getId())
                .crewId(domain.getCrewId())
                .creatorId(domain.getCreatorId())
                .title(domain.getTitle())
                .runType(domain.getRunType())
                .meetingAt(domain.getMeetingAt())
                .locationEntity(LocationEntity.builder()
                        .name(domain.getLocation().getName())
                        .address(domain.getLocation().getAddress())
                        .detailedLocation(domain.getLocation().getDetailedLocation())
                        .latitude(domain.getLocation().getLatitude())
                        .longitude(domain.getLocation().getLongitude())
                        .build())
                .description(domain.getDescription())
                .distance(domain.getDistance())
                .pace(domain.getPace())
                .maxParticipants(domain.getMaxParticipants())
                .supplies(domain.getSupplies().stream()
                        .map(content -> CrewScheduleSupplyEntity.builder()
                                .content(content)
                                .build())
                        .collect(Collectors.toList()))
                .status(domain.getStatus())
                .build();
    }
}
