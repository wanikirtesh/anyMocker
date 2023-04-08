package com.ideas.ngimocker.service.processor;

import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.service.G3CallbackService;
import com.ideas.ngimocker.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Log
@Service("OXI")
public class OXIDecisionRequestProcessor implements RequestProcessor {
    @Autowired
    G3CallbackService g3CallbackService;
    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<String>(processMessage(body,req),headers, HttpStatus.OK);
    }

    @Override
    public void postProcessor(MockRequest match, String body, HttpServletRequest req) {

    }

    private String processMessage(String body, HttpServletRequest req) {
        return null;
    }
}
