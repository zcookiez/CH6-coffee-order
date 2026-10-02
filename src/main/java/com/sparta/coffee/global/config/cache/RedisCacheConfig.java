package com.sparta.coffee.global.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
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

/**
 * Spring Cache(@Cacheable 등)를 Redis와 연동하기 위한 설정 클래스입니다.
 * - CacheManager를 RedisCacheManager로 등록하여 기본 캐시 저장소를 Redis로 지정합니다.
 * - 캐시 키(CacheNames)별로 만료 시간(TTL)이나 직렬화 방식 등 세부 옵션을 설정합니다.
 */
@EnableCaching
@Configuration
public class RedisCacheConfig {

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
}
