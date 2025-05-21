package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MeetingChatHandler extends TextWebSocketHandler {

    // 회의방 별 Sink 저장소
    private final Map<String, Set<WebSocketSession>> roomSinkMap = new ConcurrentHashMap<>();
    private final ObjectMapper customMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(Objects.requireNonNull(session.getUri()));

        roomSinkMap
                .computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // JSON -> ChatMessage
        ChatMessage chat = customMapper.readValue(message.getPayload(), ChatMessage.class);

        String json = customMapper.writeValueAsString(chat);

        roomSinkMap.getOrDefault(chat.roomId(), Collections.emptySet())
                .forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.info("Error while sending message to client: {}", e.getMessage());
                    }
                });
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
        }
    }

    private String extractRoomId(URI uri) {
        String path = uri.getPath(); // chat/rooms/abc123
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
