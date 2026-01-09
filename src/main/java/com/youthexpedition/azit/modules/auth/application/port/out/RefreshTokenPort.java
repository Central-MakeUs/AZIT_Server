package com.youthexpedition.azit.modules.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {
    void save(Long memberId, String refreshToken, long duration);
    Optional<String> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
}
