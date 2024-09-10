package com.wanikirtesh.anymocker.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        MessageWebSocketHandler.sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
        MessageWebSocketHandler.sessions.remove(session);
    }

    public static void broadcast(final String message) {
        synchronized (MessageWebSocketHandler.sessions) {
            for (final WebSocketSession session : MessageWebSocketHandler.sessions) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (final Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
    }
}
