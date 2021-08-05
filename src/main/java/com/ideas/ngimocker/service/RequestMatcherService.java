package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;

    @Autowired
    MockRequestService mockRequestService;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, MockRequest> mapExpectations = new HashMap<>();

    @PostConstruct
    public void init() {
       mockRequestService.getRequestList().forEach(mockRequest -> mapExpectations.put(mockRequest.getLabel(), mockRequest));
    }

    public MockRequest match(HttpServletRequest req, Map<String, String> queryParams,Object body) {
        String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        for (String labels : mapExpectations.keySet()) {
            MockRequest mockRequest1 = mapExpectations.get(labels);
            String pattern = mockRequest1.getUrl();
            if(matcher.match(pattern,url) && queryParams.keySet().containsAll(mockRequest1.getQueryParam())){
                logger.info("Request "+req.getMethod()+" Matched "+url+"?"+queryParams.keySet().stream().reduce("",(c,k)->"&"+k+"="+queryParams.get(k))+" with label:"+mockRequest1.getLabel()+" body:"+(body!=null?body.toString():"") );
                MockRequest mockRequest = new MockRequest();
                mockRequest.setRequestPathParams(matcher.extractUriTemplateVariables(pattern,url));
                mockRequest.setUrl(url);
                mockRequest.setLabel(mockRequest1.getLabel());
                mockRequest.setPages(mockRequest1.isPages());
                mockRequest.setRequestQueryParams(queryParams);
                mockRequest.setOnlyOK(mockRequest1.isOnlyOK());
                mockRequest.setG3CallBack(mockRequest1.getG3CallBack());
                return mockRequest;
            }
        }
        logger.warn("No Request matched to url " + url + " query:" +queryParams + " method:" + req.getMethod() + " body:"+(body!=null?body.toString():"") );
        return null;
    }
}
