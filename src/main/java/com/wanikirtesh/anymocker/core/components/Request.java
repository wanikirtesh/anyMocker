package com.wanikirtesh.anymocker.core.components;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Log
public class Request {
    @Setter
    private String name,url,method,processor="OK_ONLY",body="",fileName;
    @Setter
    private boolean download=false;
    private final List<String> queryParam = new ArrayList<>();
    private final List<String> pathParam = new ArrayList<>();
    private final Map<String, String> requestPathParams = new HashMap<>();
    private final Map<String, String> requestQueryParams = new HashMap<>();
    @Setter
    private Map<String,String> meta;

    public Request(){
        meta = new HashMap<>();
    }
    private final Map<String ,String> responseHeaders = new HashMap<>();

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


    public String getParameter(String paramName) {
        if(requestQueryParams.containsKey(paramName)){
            return requestQueryParams.get(paramName);
        }
        if(requestPathParams.containsKey(paramName)){
            return requestPathParams.get(paramName);
        }
        return "";
    }

    public String getQueryParam(String paramName){
        if(requestQueryParams.containsKey(paramName)){
            return requestQueryParams.get(paramName);
        }
        return "";
    }

    public String getPathParam(String paramName){
        if(requestPathParams.containsKey(paramName)){
            return requestPathParams.get(paramName);
        }
        return "";
    }

    public void addMeta(Map<String ,String> meta){
        this.meta.putAll(meta);
    }

    public String getMetaValue(String key){
        //log.info(System.identityHashCode(meta)+"");
        if(meta.containsKey(key)){
            return meta.get(key);
        }
        return "";
    }

    public String getResponseHeader(String key){
        if(responseHeaders.containsKey(key)){
            return responseHeaders.get(key);
        }
        return "";
    }

    public void addResponseHeaders(Map<String ,String> headers){
        responseHeaders.putAll(headers);
    }

    public void clone(Request mockRequest) {
        this.meta.putAll(mockRequest.meta);
        this.responseHeaders.putAll(mockRequest.responseHeaders);
        this.body = mockRequest.body;
        this.name = mockRequest.name;
        this.url=mockRequest.url;
        this.processor=mockRequest.processor;
        this.method=mockRequest.method;
        this.pathParam.addAll(mockRequest.pathParam);
        this.queryParam.addAll(mockRequest.queryParam);
        this.requestQueryParams.putAll(mockRequest.requestQueryParams);
        this.requestPathParams.putAll(mockRequest.requestPathParams);
        this.download = mockRequest.download;
    }
}