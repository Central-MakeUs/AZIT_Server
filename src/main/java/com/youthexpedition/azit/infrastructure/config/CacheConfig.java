package com.youthexpedition.azit.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String LATEST_TERMS_VERSIONS = "latestTermsVersions";

    private static final Duration TERMS_CACHE_TTL = Duration.ofMinutes(10); // TTL 10분

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(LATEST_TERMS_VERSIONS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(TERMS_CACHE_TTL)
                .maximumSize(100)
                .recordStats()); // Actuator 캐시 메트릭(히트율 등) 수집용
        return cacheManager;
    }
}
