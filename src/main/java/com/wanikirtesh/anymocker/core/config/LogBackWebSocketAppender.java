package com.wanikirtesh.anymocker.core.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wanikirtesh.anymocker.core.components.CustomLogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogBackWebSocketAppender extends AppenderBase<ILoggingEvent> {
    private LogWebSocketHandler logWebSocketHandler;

    public LogBackWebSocketAppender() {
        this.logWebSocketHandler = new LogWebSocketHandler();
    }

    public LogBackWebSocketAppender(final LogWebSocketHandler logWebSocketHandler) {
        this.logWebSocketHandler = logWebSocketHandler;
    }
    @Autowired
    public void setLogWebSocketHandler(final LogWebSocketHandler logWebSocketHandler) {
        this.logWebSocketHandler = logWebSocketHandler;
    }

    @Override
    protected void append(final ILoggingEvent eventObject) {
            if (null != this.logWebSocketHandler) {
                CustomLogEvent customLogEvent = new CustomLogEvent(eventObject);
            try {
                this.logWebSocketHandler.sendMessageToClients(customLogEvent.toJson());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}