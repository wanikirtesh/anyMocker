package com.ideas.ngimocker.service.processor;

import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
@Service("NONE")
@Log
public class TestProcessor implements RequestProcessor {
    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        log.info("process");
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }
    @Override
    public void postProcess(MockRequest match, String body, HttpServletRequest req) {
        log.info("post");
    }

    @Override
    public void preProcess(MockRequest match, String body, HttpServletRequest req) {
        log.info("pree");
    }
}
