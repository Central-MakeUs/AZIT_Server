package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.PointHistory;

public interface SavePointHistoryPort {
    void save(PointHistory pointHistory);
    void deleteByMemberId(Long memberId);
}
