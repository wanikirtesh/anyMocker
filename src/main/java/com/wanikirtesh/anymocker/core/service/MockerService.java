package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Log
public class MockerService {
    @Value("${default.response.body:}")
    String defaultMessage;
    @Value("${default.response.code:404}")
    int defaultResponseCode;
    @Autowired
    RequestProcessorFactory requestProcessorFactory;

    @Autowired
    RequestFactory requestFactory;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostConstruct
    public void init(){
        Set<String> collect = requestFactory.getRequestList().stream().map(Request::getProcessor).collect(Collectors.toSet());
        for (String s : collect) {
            try{
            log.info("initializing Processor:" + s);
            requestProcessorFactory.getProcessor(s).init(requestFactory.getRequests(s).stream().filter(Request::isDownload).toList());
            }catch (Exception e) {
                e.printStackTrace();
                log.severe(e.getMessage());
                log.severe("No Processor script found for " + s);
            }
        }
    }
    public ResponseEntity<String> processRequest(Request match, String body, HttpServletRequest req) {
        if(match != null){
            RequestProcessor service = requestProcessorFactory.getProcessor(match.getProcessor());
            service.preProcess(match,body,req);

            CompletableFuture.runAsync(()->{
                    service.postProcess(match,body,req);},threadPoolTaskExecutor
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

    public void reload(){
        init();
    }
}
