package com.wanikirtesh.anymocker.core.components;

import com.wanikirtesh.anymocker.core.config.LogWebSocketHandler;
import com.wanikirtesh.anymocker.core.service.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
@Log
public class ClosureProcessor implements RequestProcessor {
    private final MethodClosure pre;
    private final MethodClosure process;
    private final MethodClosure post;
    private final MethodClosure init;
    private final MethodClosure stats;
    private final MethodClosure download;
    private final Class<LogWebSocketHandler> broadcaster;

    public ClosureProcessor(final MethodClosure pre, final MethodClosure process, final MethodClosure post, final MethodClosure init, final MethodClosure download, final MethodClosure stats) {
        this.pre = pre;
        this.process = process;
        this.post = post;
        this.init = init;
        this.download = download;
        this.stats = stats;
        broadcaster = LogWebSocketHandler.class;

    }

    @Override
    public void init(final List<Request> requests) {
        init.call(ClosureProcessor.log,requests);
    }

    @Override
    public ResponseEntity<String> process(final Request match, final String body, final HttpServletRequest req) {
        return (ResponseEntity<String>) this.process.call(ClosureProcessor.log,match,body,req);
    }

    @Override
    public void postProcess(final Request match, final String body, final HttpServletRequest req) {
        this.post.call(ClosureProcessor.log,match, body, req);
    }

    @Override
    public void preProcess(final Request match, final String body, final HttpServletRequest req) {
        this.pre.call(ClosureProcessor.log,match,body,req);
    }

    @Override
    public void downloadFixtures(final Request match) {
        this.download.call(ClosureProcessor.log,match, this.broadcaster);
    }

    @Override
    public Map getStats(final Request match) {
        return (Map) this.stats.call(ClosureProcessor.log,match);
    }
}
