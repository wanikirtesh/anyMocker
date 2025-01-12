package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        this.requestProcessorFactory.init();
        final Set<String> collect = this.requestFactory.getRequestList().stream().map(Request::getProcessor).collect(Collectors.toSet());
        for (final String s : collect) {
            this.init(s);
        }
    }
    public void init(final String name){
            try{
                MockerService.log.info("initializing Processor:{}", name);
                this.requestProcessorFactory.getProcessor(name).init(this.requestFactory.getRequests(name).stream().filter(Request::isDownload).toList());
            }catch (final Exception e) {
                e.printStackTrace();
                MockerService.log.error(e.getMessage(),e);
                MockerService.log.error("No Processor script found for {}", name, e);
            }
    }
    public ResponseEntity<Object> processRequest(final Request match, final String body, final HttpServletRequest req) {
        if(null != match){
            final RequestProcessor service = this.requestProcessorFactory.getProcessor(match.getProcessor());
            service.preProcess(match,body,req);

            CompletableFuture.runAsync(()->{
                service.postProcess(match,body,req);}, this.threadPoolTaskExecutor
            );
            final ResponseEntity<Object> response = service.process(match, body, req);
            final HttpHeaders headers = new HttpHeaders();
            headers.addAll(response.getHeaders());
            for(final String header:match.getResponseHeaders().keySet()) {
                headers.add(header,match.getResponseHeader(header));
            }
            return new ResponseEntity<>(response.getBody(),headers,response.getStatusCode());
        }
        return new ResponseEntity<>(this.defaultMessage.isEmpty()?null: this.defaultMessage,null, this.defaultResponseCode);
    }

    public void reload(){
        this.init();
    }

    public void reload(final String processor) {
        this.init(processor);
    }
}
