package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.ChatMessage;
import com.cmu02.airmeet_be.utils.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingChatRedisHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> roomSinkMap = new ConcurrentHashMap<>();
    private final ReactiveRedisTemplate<String, String> defaultRedisTemplate;
    private final ObjectMapper customMapper;
    private final Key key;
    private static final int MAX_MESSAGE_SIZE = 100;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(Objects.requireNonNull(session.getUri()));

        // 1. 세션 저장
        roomSinkMap
                .computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet())
                .add(session);

        // 2. Redis에서 과거 메시지(최신 100개) 가져온다.
        String chatRoomKey = key.getChatRoomKey(roomId); // chat-room:roomId:messages
        defaultRedisTemplate.opsForList()
                .range(chatRoomKey, 0, MAX_MESSAGE_SIZE - 1)
                .collectList()
                .doOnError(err -> log.warn("초기 로드 실패: {}", err.getMessage()))
                .subscribe(history -> {
                   Collections.reverse(history);
                    for (String json : history) {
                        try {
                            session.sendMessage(new TextMessage(json));
                        } catch (IOException e) {
                            log.warn("초기 메세지 전송 실패: {}", e.getMessage());
                        }
                        log.debug("Redis Message Send Success: (roomId={}, messageId={})", roomId, json);
                    }
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage chat = customMapper.readValue(message.getPayload(), ChatMessage.class);

        String roomId = extractRoomId(Objects.requireNonNull(session.getUri()));
        String chatRoomKey = key.getChatRoomKey(roomId); // chat-room:roomId:messages
        String json = customMapper.writeValueAsString(chat);

        // -> 먼저 브로드 캐스트
        roomSinkMap.getOrDefault(roomId, Collections.emptySet())
                .forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.warn("브로드캐스트 실패 (세션 {}): {}", s.getId(), e.getMessage());
                    }
                });

        // -> Redis 저장은 완전히 분리한다 : 예외 / 지연은 WebSocket에 영향을 주지는 않는다.
        defaultRedisTemplate.opsForList().leftPush(chatRoomKey, json)
                .flatMap(cnt -> defaultRedisTemplate.opsForList().trim(chatRoomKey, 0, MAX_MESSAGE_SIZE - 1))
                .doOnError(err -> log.warn("Redis 저장 오류 : {}", err.getMessage()))
                .subscribe();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = extractRoomId(Objects.requireNonNull(session.getUri()));
        Set<WebSocketSession> set = roomSinkMap.get(roomId);

        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                roomSinkMap.remove(roomId);
            }
        } else {
            log.warn("Session not found: {}", session.getId());
        }
    }

    private String extractRoomId(URI uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
