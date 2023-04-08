package com.ideas.ngimocker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ProcessorFactory {
    @Autowired
    private ApplicationContext context;
    public RequestProcessor getProcessor(String serviceType) {
        return context.getBean(serviceType, RequestProcessor.class);
    }
}
