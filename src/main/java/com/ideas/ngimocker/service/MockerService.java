package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@Component
@Log
public class MockerService {
    @Autowired
    ProcessorFactory processorFactory;
    public ResponseEntity<String> processRequest(MockRequest match, String body, HttpServletRequest req) throws Exception {
        if(match != null){
            RequestProcessor service = processorFactory.getProcessor(match.getProcessor());
            CompletableFuture.runAsync(()->{
                service.postProcessor(match,body,req);
            });
            return service.process(match,body,req);
        }
        return new ResponseEntity<>("success",HttpStatus.OK);
    }
}
