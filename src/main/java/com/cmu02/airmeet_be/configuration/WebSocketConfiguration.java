package com.cmu02.airmeet_be.configuration;

import com.cmu02.airmeet_be.services.MeetingChatHandler;
import com.cmu02.airmeet_be.services.MeetingChatRedisHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final MeetingChatHandler chatHandler;
    private final MeetingChatRedisHandler chatRedisHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatRedisHandler, "/ws/rooms/{roodId}")
                .setAllowedOrigins("http://localhost:3000");
    }
}
