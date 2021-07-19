package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.PathList;
import com.ideas.ngimocker.components.MockRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;

    @Autowired
    PathList pathList;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, MockRequest> mapExpectations = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
       pathList.get().forEach(mockRequest -> mapExpectations.put(mockRequest.getLabel(), mockRequest));
    }

    public MockRequest match(HttpServletRequest req, Map<String, String> queryParams,Object body) {
        String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        for (String labels : mapExpectations.keySet()) {
            MockRequest mockRequest1 = mapExpectations.get(labels);
            String pattern = mockRequest1.getUrl();
            if(matcher.match(pattern,url) && queryParams.keySet().containsAll(mockRequest1.getQueryParam())){
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
