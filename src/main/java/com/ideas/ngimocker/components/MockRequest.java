package com.ideas.ngimocker.components;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MockRequest {
    @Getter @Setter
    private String label;
    @Getter @Setter
    private String url;
    @Getter @Setter
    private String method;
    @Getter @Setter
    private String body;
    @Getter @Setter
    private boolean pages;
    @Getter @Setter
    private boolean correlation;
    @Getter @Setter
    private int size;
    @Getter @Setter
    private RequestHandler store = RequestHandler.FIXTURE;
    @Getter @Setter
    private String g3CallBack;
    @Getter
    private final List<String> queryParam = new ArrayList<>();
    @Getter
    private final List<String> pathParam = new ArrayList<>();
    @Getter
    private final Map<String, String> requestPathParams = new HashMap<>();
    @Getter
    private final Map<String, String> requestQueryParams = new HashMap<>();

    public void setQueryParam(List<String> queryParam) {
        this.queryParam.addAll(queryParam);
    }

    public void setPathParam(List<String> pathParam) {
        this.pathParam.addAll(pathParam);
    }

    public void setRequestPathParams(Map<String, String> requestPathParams) {
        this.requestPathParams.putAll(requestPathParams);
    }

    public void setRequestQueryParams(Map<String, String> requestQueryParams) {
        this.requestQueryParams.putAll(requestQueryParams);
    }

    public String getPage(){
        return this.requestQueryParams.get("page");
    }

    public String getRequestedCorrelationId() throws Exception {
        if(requestQueryParams.containsKey("correlationId")){
            return requestQueryParams.get("correlationId");
        }
        if(requestQueryParams.containsKey("statisticsCorrelationId")){
            return requestQueryParams.get("statisticsCorrelationId");
        }
        if(requestPathParams.containsKey("correlationId")){
            return requestPathParams.get("correlationId");
        }
        if(requestPathParams.containsKey("statisticsCorrelationId")){
            return requestPathParams.get("statisticsCorrelationId");
        }
        throw new Exception("No Correlation Id Found in request ");
    }

    public String getPropertyCode() {
        if(requestQueryParams.containsKey("propertyCode")){
            return requestQueryParams.get("propertyCode");
        }
        if(requestPathParams.containsKey("propertyCode")){
            return requestPathParams.get("propertyCode");
        }
        return "";
    }

    public String getClientCode() {
        if(requestQueryParams.containsKey("clientCode")){
            return requestQueryParams.get("clientCode");
        }
        if(requestPathParams.containsKey("clientCode")){
            return requestPathParams.get("clientCode");
        }
        return "";
    }
    public boolean isFixture() {
        return store==RequestHandler.FIXTURE;
    }
}