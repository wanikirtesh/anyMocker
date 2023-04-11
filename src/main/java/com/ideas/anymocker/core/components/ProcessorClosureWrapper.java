package com.ideas.anymocker.core.components;

import com.ideas.anymocker.core.service.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ProcessorClosureWrapper implements RequestProcessor {
    private final MethodClosure pre;
    private final MethodClosure process;
    private final MethodClosure post;
    private final MethodClosure init;

    private final MethodClosure download;

    public ProcessorClosureWrapper(MethodClosure pre, MethodClosure process, MethodClosure post,MethodClosure init,MethodClosure download) {
        this.pre = pre;
        this.process = process;
        this.post = post;
        this.init = init;
        this.download = download;
    }

    public void init(List<Request> requests) {
        this.init.call(requests);
    }

    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        return (ResponseEntity<String>) process.call(match,body,req);
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {
        post.call(match, body, req);
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {
        pre.call(match,body,req);
    }

    @Override
    public void downloadFixtures(Request match) {
        download.call(match);
    }
}
