package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.components.OpenApiValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.openapi4j.core.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    @Autowired
    OpenApiValidator openApiValidator;

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
                if(null==this.requestProcessorFactory.getProcessor(name)){
                    throw new RuntimeException("Processor not found for name:" + name);
                }
                this.requestProcessorFactory.getProcessor(name).init(this.requestFactory.getRequests(name).stream().filter(Request::isDownload).toList());
            }catch (Exception e) {
               // e.printStackTrace();
                MockerService.log.error("No Processor script found for {}", name);
            }
    }
    public ResponseEntity<Object> processRequest(final Request match, final String body, final HttpServletRequest req) {
        if(null != match){
            if(match.isValidate()){
                log.info("validating request: {}", match.getName());
                try {
                    final ValidationException validationException = this.openApiValidator.validateRequest(match,req, body);
                    if(null != validationException) {
                        log.error(validationException.results().toString());
                        return new ResponseEntity<>(validationException.results().toString(), HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>("success", HttpStatus.OK);
                }catch (final Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage(),e);
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
                }
            }
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
