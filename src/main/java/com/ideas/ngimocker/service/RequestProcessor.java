package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface RequestProcessor {
    ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req);
    void postProcess(MockRequest match, String body, HttpServletRequest req);

    void preProcess(MockRequest match, String body, HttpServletRequest req);
}
