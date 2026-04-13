package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.modules.member.domain.model.enums.PointHistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PointHistory {

    private final Long id;
    private final Long memberId;
    private final Long points;
    private final PointHistoryType type;
    private final Long referenceId;
    private final LocalDateTime createdAt;

    public static PointHistory ofAttendance(Long memberId, Long scheduleId, long points, LocalDateTime checkedInAt) {
        return PointHistory.builder()
                .memberId(memberId)
                .points(points)
                .type(PointHistoryType.ATTENDANCE)
                .referenceId(scheduleId)
                .createdAt(checkedInAt)
                .build();
    }

    public static PointHistory ofStoreUse(Long memberId, Long orderId, long usedPoints, LocalDateTime createdAt) {
        return PointHistory.builder()
                .memberId(memberId)
                .points(-usedPoints)
                .type(PointHistoryType.STORE_USE)
                .referenceId(orderId)
                .createdAt(createdAt)
                .build();
    }

    public static PointHistory ofStoreUseRefund(Long memberId, Long orderId, long refundPoints, LocalDateTime createdAt) {
        return PointHistory.builder()
                .memberId(memberId)
                .points(refundPoints)
                .type(PointHistoryType.STORE_USE_REFUND)
                .referenceId(orderId)
                .createdAt(createdAt)
                .build();
    }
}
