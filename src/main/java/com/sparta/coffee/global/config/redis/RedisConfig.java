package com.sparta.coffee.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 수동 제어를 위한 기본 설정 클래스입니다. (Lettuce 클라이언트 기반)
 * - Spring Cache(@Cacheable) 외에 개발자가 직접 Redis 데이터(문자열, 해시, 리스트 등)를 읽고 쓰기 위해 필요한 RedisTemplate을 빈으로 등록합니다.
 * - 데이터 저장 시 사람이 읽을 수 있는 JSON 형태로 저장되도록 직렬화(Serializer)를 설정합니다.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 캐시 키는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 값은 JSON으로 직렬화 (클래스 타입 정보 포함)
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        return template;
    }
}
