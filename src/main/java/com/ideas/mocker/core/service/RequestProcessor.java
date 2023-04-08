package com.ideas.mocker.core.service;

import com.ideas.mocker.core.components.Request;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

public interface RequestProcessor {
    @PostConstruct
    void init();
    ResponseEntity<String> process(Request match, String body, HttpServletRequest req);
    void postProcess(Request match, String body, HttpServletRequest req);
    void preProcess(Request match, String body, HttpServletRequest req);

    void downloadFixtures(Request match);
}
