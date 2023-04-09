package com.ideas.anymocker.core.components;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Request {
    @Getter @Setter
    private String name,url,method,processor="OK_ONLY",body="";
    @Getter @Setter
    private boolean download=false;
    @Getter
    private final List<String> queryParam = new ArrayList<>();
    @Getter
    private final List<String> pathParam = new ArrayList<>();
    @Getter
    private final Map<String, String> requestPathParams = new HashMap<>();
    @Getter
    private final Map<String, String> requestQueryParams = new HashMap<>();
    private final Map<String,String> meta = new HashMap<>();

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

    public String getMeta(String key){
        if(meta.containsKey(key)){
            return meta.get(key);
        }
        return "";
    }
    public Map<String,String> getMeta(){
        return meta;
    }

}