package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Log
public class RequestMatcherService {

    @Autowired
    AntPathMatcher matcher;
    @Autowired
    RequestFactory requestFactory;

    public Request match(HttpServletRequest req, Map<String, String> queryParams, Object body) {
        String url =  req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            for(Request mockRequest:requestFactory.getRequestList()){
                String pattern = mockRequest.getUrl();
                if (matcher.match(pattern, url) && queryParams.keySet().containsAll(mockRequest.getQueryParam())) {
                    String queryString = req.getQueryString();
                    log.info("Request " + req.getRequestURI()+ (queryString !=null?"?"+queryString:"") + " Matched " + url + "?" + queryParams.keySet().stream().reduce("", (c, k) -> "&" + k + "=" + queryParams.get(k)) + " with name:" + mockRequest.getName() + " processor:" + mockRequest.getProcessor() + " From:" + req.getRemoteAddr() + "("+req.getRemoteHost()+")" );
                    log.fine( "body:" + (body != null ? body.toString() : ""));
                    Request request = new Request();
                    request.clone(mockRequest);
                    request.setRequestPathParams(matcher.extractUriTemplateVariables(pattern, url));
                    request.setUrl(url);
                    request.setRequestQueryParams(queryParams);
                    return request;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        log.warning("Request not matched " + url + " query:" +queryParams + " method:" + req.getMethod() + " body:"+(body!=null?body.toString():"") );
        return null;
    }
}
