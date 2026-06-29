package com.youthexpedition.azit.modules.auth.adapter.out.persistence;

import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
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

    // Redis Cluster 도입 시의 동일 슬롯 보장을 위해 memberId를 hash tag로 묶음
    private String rtKey(Long memberId) { return REFRESH_TOKEN_PREFIX + "{" + memberId + "}"; }
    private String rtPrevKey(Long memberId) { return PREV_TOKEN_PREFIX + "{" + memberId + "}"; }

    // GET-COMPARE-SET을 원자적으로 수행
    // 저장된 RT가 expected와 일치하면 RT_PREV 저장 후 새 RT로 교체
    private static final RedisScript<Long> ROTATE_SCRIPT = RedisScript.of("""
            local stored = redis.call('GET', KEYS[1])
            if stored == ARGV[1] then
                redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[3])
                redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[4])
                return 1
            end
            return 0
            """, Long.class);

    @Override
    public void save(Long memberId, String refreshToken, long duration) {
        redisTemplate.opsForValue().set(rtKey(memberId), refreshToken, duration, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(rtKey(memberId)));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(rtKey(memberId));
    }

    @Override
    public boolean compareAndRotate(Long memberId, String expectedToken, String newToken, long prevTtlSeconds, long newTtlSeconds) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(rtKey(memberId), rtPrevKey(memberId)),
                expectedToken, newToken,
                String.valueOf(prevTtlSeconds), String.valueOf(newTtlSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public Optional<String> findPrevToken(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(rtPrevKey(memberId)));
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
