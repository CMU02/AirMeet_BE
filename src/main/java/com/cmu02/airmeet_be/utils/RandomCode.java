package com.cmu02.airmeet_be.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class RandomCode {
    private final ReactiveRedisTemplate<String, String> defaultRedisTemplate;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Integer CODE_LENGTH = 4;

    public Mono<String> generateJoinCode() {
        StringBuffer sb = new StringBuffer();
        ThreadLocalRandom rd = ThreadLocalRandom.current();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(rd.nextInt(CODE_CHARS.length())));
        }

        return defaultRedisTemplate.hasKey(sb.toString())
                .flatMap(exists -> exists ? generateJoinCode() : Mono.just(sb.toString()));
    }
}
