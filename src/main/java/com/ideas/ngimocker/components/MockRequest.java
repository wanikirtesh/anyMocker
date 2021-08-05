package com.ideas.ngimocker.components;

import java.util.List;
import java.util.Map;


public class MockRequest {
    private String label;
    private String url;
    private String method;
    private String body;
    private boolean pages;
    private boolean correlation;
    private int size;
    private boolean store=true;
    private String g3CallBack;
    private List<String> queryParam;
    private List<String> pathParam;
    private Map<String, String> requestPathParams;
    private boolean onlyOK=false;

    public boolean isOnlyOK() {
        return onlyOK;
    }

    public void setOnlyOK(boolean onlyOK) {
        this.onlyOK = onlyOK;
    }

    public Map<String, String> getRequestPathParams() {
        return requestPathParams;
    }

    public Map<String, String> getRequestQueryParams() {
        return requestQueryParams;
    }

    private Map<String, String> requestQueryParams;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isPages() {
        return pages;
    }

    public void setPages(boolean pages) {
        this.pages = pages;
    }

    public boolean isCorrelation() {
        return correlation;
    }

    public void setCorrelation(boolean correlation) {
        this.correlation = correlation;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isStore() {
        return this.store;
    }

    public void setStore(boolean store) {
        this.store = store;
    }

    public String getG3CallBack() {
        return g3CallBack;
    }

    public void setG3CallBack(String g3CallBack) {
        this.g3CallBack = g3CallBack;
    }

    public List<String> getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(List<String> queryParam) {
        this.queryParam = queryParam;
    }

    public List<String> getPathParam() {
        return pathParam;
    }

    public void setPathParam(List<String> pathParam) {
        this.pathParam = pathParam;
    }

    public void setRequestPathParams(Map<String, String> requestPathParams) {

        this.requestPathParams = requestPathParams;
    }

    public void setRequestQueryParams(Map<String, String> requestQueryParams) {
        this.requestQueryParams = requestQueryParams;
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
}