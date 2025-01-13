package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Slf4j
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;
    @Autowired
    RequestFactory requestFactory;

    public Request match(final HttpServletRequest req, final Map<String, String> queryParams, final Object body) {
        final String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            for(final Request mockRequest: this.requestFactory.getRequestList()){
                final String pattern = mockRequest.getUrl();
                if (this.matcher.match(pattern, url) && queryParams.keySet().containsAll(mockRequest.getQueryParam()) && mockRequest.getMethod().equalsIgnoreCase(req.getMethod())) {
                    final String queryString = req.getQueryString();
                    RequestMatcherService.log.info("Request " + req.getRequestURI()+ (null != queryString ?"?"+queryString:"") + " Matched " + url + "?" + queryParams.keySet().stream().reduce("", (c, k) -> "&" + k + "=" + queryParams.get(k)) + " with name:" + mockRequest.getName() + " processor:" + mockRequest.getProcessor() + " From:" + req.getRemoteAddr() + "("+req.getRemoteHost()+")" );
                    RequestMatcherService.log.debug( "body:" + (null != body ? body.toString() : ""));
                    final Request request = new Request();
                    request.clone(mockRequest);
                    request.setRequestPathParams(this.matcher.extractUriTemplateVariables(pattern, url));
                    request.setUrl(url);
                    request.setRequestQueryParams(queryParams);
                    return request;
                }
            }
        }catch (final Exception e){
            e.printStackTrace();
        }
        RequestMatcherService.log.warn("Request not matched " + url + " query:" +queryParams + " method:" + req.getMethod() + " body:"+(null != body ?body.toString():"") );
        return null;
    }

    public Request isMatchedAny(Request req) {
        for (Request request : requestFactory.getRequestList()) {
            if(matcher.match(request.getUrl(), req.getUrl()) && matcher.match(request.getMethod(), req.getMethod())) {
                return request;
            }
        }
        return null;
    }
}
