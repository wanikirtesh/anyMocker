package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface RequestProcessor {

    void init();
    ResponseEntity<String> process(Request match, String body, HttpServletRequest req);
    void postProcess(Request match, String body, HttpServletRequest req);
    void preProcess(Request match, String body, HttpServletRequest req);

    void downloadFixtures(Request match);

    Map getStats(Request match);
}
