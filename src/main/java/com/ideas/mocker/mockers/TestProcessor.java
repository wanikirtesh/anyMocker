package com.ideas.mocker.mockers;

import com.ideas.mocker.core.components.Request;
import com.ideas.mocker.core.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@Service("TEST")
@Log
public class TestProcessor implements RequestProcessor {
    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        log.info("process");
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }
    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {
        log.info("post");
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {
        log.info("pree");
    }

    @Override
    public void downloadFixtures(Request match) {

    }
}
