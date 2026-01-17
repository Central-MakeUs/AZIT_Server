package com.youthexpedition.azit.modules.auth.adapter.out.persistence;

import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenAdapter implements TokenPort {
    private final StringRedisTemplate redisTemplate;
    private static final String RT_PREFIX = "RT:";

    @Override
    public void save(Long memberId, String refreshToken, long duration) {
        redisTemplate.opsForValue().set("RT:" + memberId, refreshToken, duration, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findByMemberId(Long memberId) {
        String token = redisTemplate.opsForValue().get(RT_PREFIX + memberId);
        return Optional.ofNullable(token);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(RT_PREFIX + memberId);
    }
}
