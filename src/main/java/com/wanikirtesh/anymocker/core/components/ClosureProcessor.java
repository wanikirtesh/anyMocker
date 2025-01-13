package com.wanikirtesh.anymocker.core.components;

import com.wanikirtesh.anymocker.core.service.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Slf4j
public class ClosureProcessor implements RequestProcessor {
    private final MethodClosure pre;
    private final MethodClosure process;
    private final MethodClosure post;
    private final MethodClosure init;
    private final MethodClosure stats;
    private final MethodClosure download;

    public ClosureProcessor(final MethodClosure pre, final MethodClosure process, final MethodClosure post, final MethodClosure init, final MethodClosure download, final MethodClosure stats) {
        this.pre = pre;
        this.process = process;
        this.post = post;
        this.init = init;
        this.download = download;
        this.stats = stats;
    }

    @Override
    public void init(final List<Request> requests) {
        init.call(log,requests);
    }

    @Override
    public ResponseEntity<Object> process(final Request match, final String body, final HttpServletRequest req) {
        return (ResponseEntity<Object>) this.process.call(ClosureProcessor.log,match,body,req);
    }

    @Override
    public void postProcess(final Request match, final String body, final HttpServletRequest req) {
        this.post.call(log,match, body, req);
    }

    @Override
    public void preProcess(final Request match, final String body, final HttpServletRequest req) {
        pre.call(log,match,body,req);
    }

    @Override
    public void downloadFixtures(final Request match) {
        download.call(log,match);
    }

    @Override
    public Map getStats(final Request match) {
        try {
            return (Map) stats.call(log, match);
        }catch (Exception e){
            log.error(stats.getDelegate().getClass().getName() + ":" +stats.getMethod() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
