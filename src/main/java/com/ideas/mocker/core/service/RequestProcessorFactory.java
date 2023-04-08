package com.ideas.mocker.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class RequestProcessorFactory {
    @Autowired
    private ApplicationContext context;
    public RequestProcessor getProcessor(String serviceType) {
        return context.getBean(serviceType, RequestProcessor.class);
    }
}
