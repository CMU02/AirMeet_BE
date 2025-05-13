package com.cmu02.airmeet_be.configuration;

import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // Redis Lettuce Driver Connection Bean 등록
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Connection Modes 에서 Standalone 모드 사용
        RedisStandaloneConfiguration standaloneconfig = new RedisStandaloneConfiguration("127.0.0.1", 6379);

        // Lettuce 클라이언트 설정 코드
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                // SSL/TLS 보안연결(암호화된 통신)을 사용하도록 설정
                // 개발버전은 사용하지 않음
                // .useSsl().and()
                .commandTimeout(Duration.ofSeconds(2)) // 명령을 보낸후 최대 2초 기다림
                .shutdownTimeout(Duration.ZERO) // 클라이언트 종료할 때 최대 대기 시간은 0초 즉, 즉시 종료
                .build();


        return new LettuceConnectionFactory(standaloneconfig, clientConfig);
    }

    // ReactiveRedisTemplate Bean 등록
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer()) // Object 직렬화
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    // MeetingRoom ReactiveRedisTemplate Bean 등록
    @Bean
    public ReactiveRedisTemplate<String, MeetingRoom> meetingRoomReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 출력

        Jackson2JsonRedisSerializer<MeetingRoom> serializer = new Jackson2JsonRedisSerializer<>(mapper, MeetingRoom.class);

        RedisSerializationContext<String, MeetingRoom> context = RedisSerializationContext
                .<String, MeetingRoom>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
