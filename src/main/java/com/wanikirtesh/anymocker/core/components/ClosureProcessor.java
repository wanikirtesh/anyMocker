package com.wanikirtesh.anymocker.core.components;

import com.wanikirtesh.anymocker.core.config.LogWebSocketHandler;
import com.wanikirtesh.anymocker.core.service.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ClosureProcessor implements RequestProcessor {
    private final Logger log;
    private final MethodClosure pre;
    private final MethodClosure process;
    private final MethodClosure post;
    private final MethodClosure init;
    private final MethodClosure stats;
    private final MethodClosure download;
    private final Class<LogWebSocketHandler> broadcaster;

    public ClosureProcessor(Logger log, MethodClosure pre, MethodClosure process, MethodClosure post, MethodClosure init, MethodClosure download,MethodClosure stats) {
        this.log = log;
        this.pre = pre;
        this.process = process;
        this.post = post;
        this.init = init;
        this.download = download;
        this.stats = stats;
        this.broadcaster = LogWebSocketHandler.class;

    }

    public void init(List<Request> requests) {
        this.init.call(log,requests);
    }

    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        return (ResponseEntity<String>) process.call(log,match,body,req);
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {
        post.call(log,match, body, req);
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {
        pre.call(log,match,body,req);
    }

    @Override
    public void downloadFixtures(Request match) {
        download.call(log,match,broadcaster);
    }

    @Override
    public Map getStats(Request match) {
        return (Map) stats.call(log,match);
    }
}
