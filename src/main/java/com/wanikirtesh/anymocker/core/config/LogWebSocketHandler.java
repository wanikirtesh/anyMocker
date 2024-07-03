package com.wanikirtesh.anymocker.core.config;

import lombok.extern.java.Log;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Log
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        LogWebSocketHandler.sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
        LogWebSocketHandler.sessions.remove(session);
    }

    public static void broadcast(final String message) {
        synchronized (LogWebSocketHandler.sessions) {
            for (final WebSocketSession session : LogWebSocketHandler.sessions) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (final Exception e) {
                    LogWebSocketHandler.log.severe(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
