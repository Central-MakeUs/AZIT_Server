package com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleMemberEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewScheduleSupplyEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.LocationEntity;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.CrewScheduleMember;
import com.youthexpedition.azit.modules.crew.domain.model.Location;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
                        .placeName(entity.getLocationEntity().getPlaceName())
                        .address(entity.getLocationEntity().getAddress())
                        .meetingSpot(entity.getLocationEntity().getMeetingSpot())
                        .latitude(entity.getLocationEntity().getLatitude())
                        .longitude(entity.getLocationEntity().getLongitude())
                        .build())
                .description(entity.getDescription())
                .distance(entity.getDistance())
                .pace(entity.getPace())
                .maxParticipants(entity.getMaxParticipants())
                .supplies(entity.getSupplies().stream()
                        .map(CrewScheduleSupplyEntity::getContent)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .participants(entity.getMembers().stream()
                        .collect(Collectors.toMap(
                                CrewScheduleMemberEntity::getMemberId,
                                m -> CrewScheduleMember.builder()
                                        .id(m.getId())
                                        .memberId(m.getMemberId())
                                        .isCheckedIn(m.isCheckedIn())
                                        .checkedInAt(m.getCheckedInAt())
                                        .createdAt(m.getCreatedAt())
                                        .build(),
                                (existing, replacement) -> existing,
                                LinkedHashMap::new
                        )))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // 신규 생성
    public CrewScheduleEntity toEntity(CrewSchedule domain) {
        if (domain == null) return null;

        CrewScheduleEntity scheduleEntity = CrewScheduleEntity.builder()
                .id(domain.getId())
                .crewId(domain.getCrewId())
                .creatorId(domain.getCreatorId())
                .title(domain.getTitle())
                .runType(domain.getRunType())
                .meetingAt(domain.getMeetingAt())
                .locationEntity(LocationEntity.builder()
                        .placeName(domain.getLocation().getPlaceName())
                        .address(domain.getLocation().getAddress())
                        .meetingSpot(domain.getLocation().getMeetingSpot())
                        .latitude(domain.getLocation().getLatitude())
                        .longitude(domain.getLocation().getLongitude())
                        .build())
                .description(domain.getDescription())
                .distance(domain.getDistance())
                .pace(domain.getPace())
                .maxParticipants(domain.getMaxParticipants())
                .status(domain.getStatus())
                .build();

        if (domain.getSupplies() != null) {
            domain.getSupplies().stream()
                    .map(content -> CrewScheduleSupplyEntity.builder()
                            .content(content)
                            .schedule(scheduleEntity)
                            .build())
                    .forEach(scheduleEntity::addSupply);
        }

        if (domain.getParticipantIds() != null) {
            domain.getParticipantIds().forEach(scheduleEntity::addMember);
        }

        return scheduleEntity;
    }

    // 업데이트
    public void updateEntity(CrewScheduleEntity entity, CrewSchedule domain) {
        entity.syncWithDomain(
                domain.getTitle(),
                domain.getRunType(),
                domain.getMeetingAt(),
                domain.getLocation().getPlaceName(),
                domain.getLocation().getAddress(),
                domain.getLocation().getMeetingSpot(),
                domain.getLocation().getLatitude(),
                domain.getLocation().getLongitude(),
                domain.getDescription(),
                domain.getDistance(),
                domain.getPace(),
                domain.getMaxParticipants(),
                domain.getStatus()
        );

        // 준비물 업데이트
        List<String> currentSupplies = entity.getSupplies().stream()
                .map(CrewScheduleSupplyEntity::getContent)
                .toList();

        // 도메인 리스트에 없는 준비물만 엔티티에서 제거
        entity.getSupplies().removeIf(s -> !domain.getSupplies().contains(s.getContent()));

        // 엔티티에 아직 없는 준비물만 새로 추가
        domain.getSupplies().stream()
                .filter(content -> !currentSupplies.contains(content))
                .map(content -> CrewScheduleSupplyEntity.builder()
                        .content(content)
                        .schedule(entity)
                        .build())
                .forEach(entity::addSupply);

        // 참여 멤버 업데이트
        List<Long> domainMemberIds = domain.getParticipantIds();

        // 도메인에 없는 멤버 삭제
        entity.getMembers().removeIf(m -> !domainMemberIds.contains(m.getMemberId()));

        // 기존 멤버의 출석 상태 동기화
        entity.getMembers().forEach(memberEntity -> {
            CrewScheduleMember crewScheduleMember = domain.getParticipants().get(memberEntity.getMemberId());
            if (crewScheduleMember != null) {
                memberEntity.syncCheckIn(crewScheduleMember.isCheckedIn(), crewScheduleMember.getCheckedInAt());
            }
        });

        // 새로 추가된 멤버 등록
        List<Long> currentMemberIds = entity.getMembers().stream().map(CrewScheduleMemberEntity::getMemberId).toList();
        domainMemberIds.stream()
                .filter(id -> !currentMemberIds.contains(id))
                .forEach(entity::addMember);
    }
}
