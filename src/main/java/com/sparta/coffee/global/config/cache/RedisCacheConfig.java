package com.sparta.coffee.global.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableCaching
@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
        // 메뉴 목록은 변경 시 즉시 Evict 처리하므로 TTL을 길게 잡거나 무제한으로 설정
        configurations.put(CacheNames.MENUS_ALL, defaultConfig.entryTtl(Duration.ofDays(1))); 
        // 인기 메뉴는 하루 단위(자정)로 갱신되므로 1일 유지
        configurations.put(CacheNames.POPULAR_MENUS, defaultConfig.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configurations)
                .build();
    }

    /**
     * Redis 장애 시 어플리케이션 전체 장애로 번지지 않고, 
     * 원본 메서드(DB 직접 조회)를 실행하게 만드는 진짜 Fallback 안전망
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis GET 에러 발생! 캐시 우회(Fallback)하여 원본 DB 메서드를 실행합니다. Cache: {}, Key: {}", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis PUT 에러 발생! Cache: {}, Key: {}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis EVICT 에러 발생! Cache: {}, Key: {}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis CLEAR 에러 발생! Cache: {}", cache.getName(), exception);
            }
        };
    }
}
