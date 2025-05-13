package com.cmu02.airmeet_be.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    // Redis Lettuce Driver Connection Bean 등록
    public LettuceConnectionFactory redisConnectionFactory() {
        // Connection Modes 에서 Sentinel 모드 사용
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.master("airmeet-master") // Master Name
                // host, port
                .sentinel("127.0.0.1", 26379)
                .sentinel("127.0.0.1", 26380);

        // Lettuce 클라이언트 설정 코드
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                // SSL/TLS 보안연결(암호화된 통신)을 사용하도록 설정
                // 개발버전은 사용하지 않음
                // .useSsl().and()
                .commandTimeout(Duration.ofSeconds(2)) // 명령을 보낸후 최대 2초 기다림
                .shutdownTimeout(Duration.ZERO) // 클라이언트 종료할 때 최대 대기 시간은 0초 즉, 즉시 종료
                .build();


        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean
    // RedisTemplate Bean 비동기 방식 등록
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer()) // Object 직렬화
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
