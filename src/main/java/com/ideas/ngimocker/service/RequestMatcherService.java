package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@Log
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;

    @Autowired
    MockRequestMapper mockRequestMapper;

    private final Map<String, MockRequest> mapExpectations = new HashMap<>();

    @PostConstruct
    public void init() {
       mockRequestMapper.getRequestList().forEach(mockRequest -> mapExpectations.put(mockRequest.getLabel(), mockRequest));
    }

    public MockRequest match(HttpServletRequest req, Map<String, String> queryParams,Object body) {
        String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            for (String labels : mapExpectations.keySet()) {
                MockRequest comingRequest = mapExpectations.get(labels);
                String pattern = comingRequest.getUrl();
                if (matcher.match(pattern, url) && queryParams.keySet().containsAll(comingRequest.getQueryParam())) {
                    log.info("Request " + req.getMethod() + " Matched " + url + "?" + queryParams.keySet().stream().reduce("", (c, k) -> "&" + k + "=" + queryParams.get(k)) + " with label:" + comingRequest.getLabel() );
                    log.fine( "body:" + (body != null ? body.toString() : ""));
                    MockRequest mockRequest = new MockRequest();
                    mockRequest.setRequestPathParams(matcher.extractUriTemplateVariables(pattern, url));
                    mockRequest.setUrl(url);
                    mockRequest.setLabel(comingRequest.getLabel());
                    mockRequest.setPages(comingRequest.isPages());
                    mockRequest.setRequestQueryParams(queryParams);
                    mockRequest.setG3CallBack(comingRequest.getG3CallBack());
                    mockRequest.setStore(comingRequest.getStore());
                    return mockRequest;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        log.warning("No Request matched to url " + url + " query:" +queryParams + " method:" + req.getMethod() + " body:"+(body!=null?body.toString():"") );
        return null;
    }
}
