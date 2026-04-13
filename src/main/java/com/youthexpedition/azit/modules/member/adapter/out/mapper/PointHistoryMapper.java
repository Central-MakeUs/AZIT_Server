package com.youthexpedition.azit.modules.member.adapter.out.mapper;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.PointHistoryEntity;
import com.youthexpedition.azit.modules.member.domain.model.PointHistory;
import org.springframework.stereotype.Component;

@Component
public class PointHistoryMapper {

    public PointHistoryEntity toEntity(PointHistory domain) {
        return PointHistoryEntity.builder()
                .memberId(domain.getMemberId())
                .points(domain.getPoints())
                .type(domain.getType())
                .referenceId(domain.getReferenceId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public PointHistory toDomain(PointHistoryEntity entity) {
        return PointHistory.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .points(entity.getPoints())
                .type(entity.getType())
                .referenceId(entity.getReferenceId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
