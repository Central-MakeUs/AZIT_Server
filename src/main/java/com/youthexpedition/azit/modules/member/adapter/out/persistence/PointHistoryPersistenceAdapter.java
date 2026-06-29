package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.mapper.PointHistoryMapper;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.PointHistoryRepository;
import com.youthexpedition.azit.modules.member.application.port.out.SavePointHistoryPort;
import com.youthexpedition.azit.modules.member.domain.model.PointHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointHistoryPersistenceAdapter implements SavePointHistoryPort {

    private final PointHistoryRepository pointHistoryRepository;
    private final PointHistoryMapper pointHistoryMapper;

    @Override
    public void save(PointHistory pointHistory) {
        pointHistoryRepository.save(pointHistoryMapper.toEntity(pointHistory));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        pointHistoryRepository.deleteByMemberId(memberId);
    }
}
