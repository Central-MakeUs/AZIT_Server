package com.youthexpedition.azit.modules.auth.application.port.out;

import java.util.Optional;

public interface TokenPort {
    void save(Long memberId, String refreshToken, long duration);
    Optional<String> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
    void addToBlacklist(String accessToken, String reason);
    boolean isBlacklisted(String accessToken);
}
