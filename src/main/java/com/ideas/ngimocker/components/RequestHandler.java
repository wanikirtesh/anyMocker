package com.ideas.ngimocker.components;

import com.ideas.ngimocker.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Log
@Component
public enum RequestHandler {
    FIXTURE {
        @Override
        public ResponseEntity<String> processRequest(MockRequest match, String body, Map<String, RequestProcessor> processorMap, HttpServletRequest req) {
            log.info("getting matching fixtures");
            return processorMap.get("fixture").process(match,body,req);

        }
    }, HTNG {
        @Override
        public ResponseEntity<String> processRequest(MockRequest match, String body, Map<String, RequestProcessor> processorMap, HttpServletRequest req) {
            return processorMap.get("htng").process(match,body,req);
        }
    },NONE {
        @Override
        public ResponseEntity<String> processRequest(MockRequest match, String body, Map<String, RequestProcessor> processorMap, HttpServletRequest req) {
            return null;
        }
    },OK_ONLY {
        @Override
        public ResponseEntity<String> processRequest(MockRequest match, String body, Map<String, RequestProcessor> processorMap, HttpServletRequest req) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    };



    public abstract ResponseEntity<String> processRequest(MockRequest match, String body, Map<String, RequestProcessor> processorMap, HttpServletRequest req) throws Exception;
}
