package com.ideas.anymocker.core.service;

import com.ideas.anymocker.core.components.Request;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Log
public class NewMockerService {
    @Value("${default.response.body:}")
    String defaultMessage;
    @Value("${default.response.code:404}")
    int defaultResponseCode;
    @Autowired
    NewRequestProcessorFactory requestProcessorFactory;
    @Value("${use.groovy.impl:true}")
    private boolean useGroovy;

    @Autowired
    RequestFactory requestFactory;

    @PostConstruct
    public void init(){
        if(useGroovy){Set<String> collect = requestFactory.getRequestList().stream().map(Request::getProcessor).collect(Collectors.toSet());
        for (String s : collect) {
            try{
            log.info("initializing Processor:" + s);
            requestProcessorFactory.getProcessor(s).init(requestFactory.getRequests(s).stream().filter(Request::isDownload).collect(Collectors.toList()));
            }catch (Exception e) {
                log.severe("No Processor script found for " + s);
            }
        }
        }
    }
    public ResponseEntity<String> processRequest(Request match, String body, HttpServletRequest req) {
        if(match != null){
            RequestProcessor service = requestProcessorFactory.getProcessor(match.getProcessor());
            service.preProcess(match,body,req);
            CompletableFuture.runAsync(()->
                    service.postProcess(match,body,req)
            );
            ResponseEntity<String> response = service.process(match, body, req);
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(response.getHeaders());
            for(String header:match.getResponseHeaders().keySet()) {
                headers.add(header,match.getResponseHeader(header));
            }
            return new ResponseEntity<>(response.getBody(),headers,response.getStatusCode());
        }
        return new ResponseEntity<>(defaultMessage.isEmpty()?null:defaultMessage,null,defaultResponseCode);
    }
}
