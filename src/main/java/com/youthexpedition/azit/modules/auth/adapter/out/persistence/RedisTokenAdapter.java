package com.youthexpedition.azit.modules.auth.adapter.out.persistence;

import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenAdapter implements TokenPort {
    private final StringRedisTemplate redisTemplate;
    private final TokenProviderPort tokenProviderPort;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String PREV_TOKEN_PREFIX = "RT_PREV:";
    private static final String BLACKLIST_PREFIX = "BL:";

    @Override
    public void save(Long memberId, String refreshToken, long duration) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + memberId, refreshToken, duration, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findByMemberId(Long memberId) {
        String token = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + memberId);
        return Optional.ofNullable(token);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
    }

    @Override
    public void savePrevToken(Long memberId, String prevToken, long ttlSeconds) { // race condition 시 로그아웃 방어하기 위해 10초 동안 임시 저장
        redisTemplate.opsForValue().set(PREV_TOKEN_PREFIX + memberId, prevToken, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findPrevToken(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREV_TOKEN_PREFIX + memberId));
    }

    @Override
    public void addToBlacklist(String accessToken, String reason) {
        long remainingTimeMillis = tokenProviderPort.getRemainingExpirationMilliseconds(accessToken);
        if (remainingTimeMillis > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken, reason, remainingTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
