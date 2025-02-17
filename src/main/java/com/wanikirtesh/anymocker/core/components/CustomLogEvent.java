package com.wanikirtesh.anymocker.core.components;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
@Getter @Setter
public class CustomLogEvent {

    private String message;

    private String loggerName;

    private String level;

    private String[] stackTrace;

    private long timestamp;
    private String processorName = "";


    public CustomLogEvent(ILoggingEvent eventObject) {
        this.message = eventObject.getFormattedMessage();
        this.loggerName = eventObject.getLoggerName();
        this.level = eventObject.getLevel().toString();
        this.timestamp = eventObject.getTimeStamp();
        this.processorName = eventObject.getMDCPropertyMap().get("processorName");

        // Extract stack trace if the event contains a throwable
        if (eventObject.getThrowableProxy() != null) {
            this.stackTrace = Arrays.stream(eventObject.getThrowableProxy().getStackTraceElementProxyArray())
                    .map(stackTraceElementProxy -> stackTraceElementProxy.getSTEAsString())
                    .toArray(String[]::new);
        }
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}