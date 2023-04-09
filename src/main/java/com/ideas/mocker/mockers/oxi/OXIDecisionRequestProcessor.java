package com.ideas.mocker.mockers.oxi;

import com.ideas.mocker.core.components.Request;
import com.ideas.mocker.core.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Log
@Service("OXI")
public class OXIDecisionRequestProcessor implements RequestProcessor {


    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<>(processMessage(body,req),headers, HttpStatus.OK);
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void downloadFixtures(Request match) {

    }

    private String processMessage(String body, HttpServletRequest req) {
        return null;
    }
}
