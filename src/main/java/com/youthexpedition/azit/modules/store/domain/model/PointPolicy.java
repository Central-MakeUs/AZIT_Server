package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PointPolicy {
    public static final long MIN_POINT_USAGE = 1000L; // 최소 1,000P부터
    public static final long POINT_UNIT = 100L;      // 100P 단위로 사용 가능

    // 포인트 사용 가능 여부 확인
    public static void validate(Member member, long usePoints) {
        // 0포인트 사용 시 패스
        if (usePoints <= 0) {
            return;
        }

        // 보유 포인트 초과 여부 확인
        if (usePoints > member.getTotalPoints()) {
            throw new BusinessException(StoreErrorCode.EXCEED_AVAILABLE_POINTS);
        }

        // 최소 사용 금액 확인 (1,000P 미만인 경우)
        if (usePoints < MIN_POINT_USAGE) {
            throw new BusinessException(StoreErrorCode.BELOW_MIN_POINT_USAGE);
        }

        // 사용 단위 확인 (100P 단위가 아닌 경우)
        if (usePoints % POINT_UNIT != 0) {
            throw new BusinessException(StoreErrorCode.INVALID_POINT_UNIT);
        }
    }
}
