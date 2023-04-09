package com.ideas.anymocker.core.service;

import com.ideas.anymocker.core.components.Request;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@Log
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;
    @Autowired
    RequestFactory requestFactory;
    private final Map<String, Request> mapExpectations = new HashMap<>();
    @PostConstruct
    public void init() {
       requestFactory.getRequestList().forEach(mockRequest -> mapExpectations.put(mockRequest.getName(), mockRequest));
    }

    public Request match(HttpServletRequest req, Map<String, String> queryParams, Object body) {
        String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            for (String labels : mapExpectations.keySet()) {
                Request comingRequest = mapExpectations.get(labels);
                String pattern = comingRequest.getUrl();
                if (matcher.match(pattern, url) && queryParams.keySet().containsAll(comingRequest.getQueryParam())) {
                    log.info("Request " + req.getMethod() + " Matched " + url + "?" + queryParams.keySet().stream().reduce("", (c, k) -> "&" + k + "=" + queryParams.get(k)) + " with name:" + comingRequest.getName() + " store:" + comingRequest.getProcessor());
                    log.fine( "body:" + (body != null ? body.toString() : ""));
                    Request request = new Request();
                    request.setRequestPathParams(matcher.extractUriTemplateVariables(pattern, url));
                    request.setUrl(url);
                    request.setMethod(comingRequest.getMethod());
                    request.setName(comingRequest.getName());
                    request.addMeta(comingRequest.getMeta());
                    request.setRequestQueryParams(queryParams);
                    request.setDownload(comingRequest.isDownload());
                    request.setProcessor(comingRequest.getProcessor());
                    request.setBody(comingRequest.getBody());
                    return request;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        log.warning("No Request matched to url " + url + " query:" +queryParams + " method:" + req.getMethod() + " body:"+(body!=null?body.toString():"") );
        return null;
    }
}
