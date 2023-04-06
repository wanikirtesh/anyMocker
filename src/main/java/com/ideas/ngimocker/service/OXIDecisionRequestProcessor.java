package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Log
@Service
public class OXIDecisionRequestProcessor implements RequestProcessor{
    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<String>(processMessage(body,req),headers, HttpStatus.OK);
    }

    private String processMessage(String body, HttpServletRequest req) {
        return null;
    }
}
