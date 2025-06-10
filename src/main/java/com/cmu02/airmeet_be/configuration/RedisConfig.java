package com.cmu02.airmeet_be.configuration;

import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
                 .useSsl().and()
                .commandTimeout(Duration.ofSeconds(2)) // 명령을 보낸후 최대 2초 기다림
                .shutdownTimeout(Duration.ZERO) // 클라이언트 종료할 때 최대 대기 시간은 0초 즉, 즉시 종료
                .build();

        return new LettuceConnectionFactory(standaloneconfig, clientConfig);
    }

    // MeetingRoom RedisTemplate Bean 등록
    @Bean
    public ReactiveRedisTemplate<String, MeetingRoom> meetingRoomRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper customMapper
    ) {
        return new ReactiveRedisTemplate<>(factory, getContext(customMapper, MeetingRoom.class));
    }

    // JoinCode with MeetingRoomId RedisTemplate Bean 등록
    @Bean
    public ReactiveRedisTemplate<String, String> defaultRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    // User RedisTemplate Bean 등록
    @Bean
    public ReactiveRedisTemplate<String, User> userRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper customMapper
    ) {
        return new ReactiveRedisTemplate<>(factory, getContext(customMapper, User.class));
    }

    /**
     * JackSon2JsonRedisSerializer를 생성하는 메서드
     * @param mapper Jackson ObjectMapper - 직렬화/역직렬화를 위한 매퍼 객체
     * @param type 직렬화/역직렬화할 대상 클래스 타입
     * @param <V> 직렬화 대상 객체의 대상 타입
     * @return Jackson2JsonRedisSerializer<V> 지정된 타입에 대한 Json 기반 Redis 직렬화
     */
    private <V> Jackson2JsonRedisSerializer<V> getSerializer(ObjectMapper mapper, Class<V> type) {
        // 주어진 클래스 타입을 기반으로 Redis에 저장할 때 사용할 JSON 직렬화기를 생성함
        return new Jackson2JsonRedisSerializer<>(mapper, type);
    }

    /**
     * RedisSerializationContext 생성 메서드
     * RedisTemplate 또는 ReactiveRedisTemplate을 사용할 때,
     * Key와 Value 각각에 대한 직렬화 방식을 정의하는 컨텍스트를 생성함.
     * @param mapper Jackson ObjectMapper - value 직렬화에 사용될 매퍼 객체
     * @param valueType Redis에 저장할 Value 객체의 클래스 타입
     * @param <V> Redis에 저장될 Value 타입
     * @return RedisSerializationContext<String, V> - key는 문자열, value는 V 타입인 직렬화 컨텍스트
     */
    private <V> RedisSerializationContext<String, V> getContext(ObjectMapper mapper, Class<V> valueType) {
        // Key는 문자열로 저장되므로 StringRedisSerializer 사용
        // Value는 Jackson2JsonRedisSerializer를 이용하여 JSON 직렬화
        return RedisSerializationContext.<String, V>newSerializationContext(new StringRedisSerializer())
                .value(getSerializer(mapper, valueType)) // value 직렬화 설정
                .build(); // 최종 Redis 직렬화 컨텍스트 빌드
    }

}
