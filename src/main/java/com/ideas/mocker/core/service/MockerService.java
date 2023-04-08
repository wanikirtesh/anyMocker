package com.ideas.mocker.core.service;

import com.ideas.mocker.core.components.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@Component
@Log
public class MockerService {

    @Value("${default.response.body:}")
    String defaultMessage;
    @Value("${default.response.code:404}")
    int defaultResponseCode;
    @Autowired
    RequestProcessorFactory requestProcessorFactory;
    public ResponseEntity<String> processRequest(Request match, String body, HttpServletRequest req) {
        if(match != null){
            RequestProcessor service = requestProcessorFactory.getProcessor(match.getProcessor());
            service.preProcess(match,body,req);
            CompletableFuture.runAsync(()->
                service.postProcess(match,body,req)
            );
            return service.process(match,body,req);
        }
        return new ResponseEntity<>(defaultMessage.isEmpty()?null:defaultMessage,null,defaultResponseCode);
    }
}
