package com.wanikirtesh.anymocker.core.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
    @Setter
    private String name, url, method, processor = "OK_ONLY",spec="";
    @Setter
    @JsonIgnore
    private String body = "", fileName = "";
    @Setter
    private boolean download, grouping = false,validate=false;
    private final List<String> queryParam = new ArrayList<>();
    private final List<String> pathParam = new ArrayList<>();
    @JsonIgnore
    private final Map<String, String> requestPathParams = new HashMap<>();
    @JsonIgnore
    private final Map<String, String> requestQueryParams = new HashMap<>();
    @Setter
    private Map<String, String> meta;
    private final Map<String, String> responseHeaders = new HashMap<>();

    public Request() {
        this.meta = new HashMap<>();
    }

    public void setQueryParam(final List<String> queryParam) {
        this.queryParam.addAll(queryParam);
    }

    public void setPathParam(final List<String> pathParam) {
        this.pathParam.addAll(pathParam);
    }

    public void setRequestPathParams(final Map<String, String> requestPathParams) {
        this.requestPathParams.putAll(requestPathParams);
    }

    public void setRequestQueryParams(final Map<String, String> requestQueryParams) {
        this.requestQueryParams.putAll(requestQueryParams);
    }


    public String getParameter(final String paramName) {
        if (this.requestQueryParams.containsKey(paramName)) {
            return this.requestQueryParams.get(paramName);
        }
        if (this.requestPathParams.containsKey(paramName)) {
            return this.requestPathParams.get(paramName);
        }
        return "";
    }

    public String getQueryParam(final String paramName) {
        if (this.requestQueryParams.containsKey(paramName)) {
            return this.requestQueryParams.get(paramName);
        }
        return "";
    }

    public String getPathParam(final String paramName) {
        if (this.requestPathParams.containsKey(paramName)) {
            return this.requestPathParams.get(paramName);
        }
        return "";
    }

    public void addMeta(final Map<String, String> meta) {
        this.meta.putAll(meta);
    }

    @JsonIgnore
    public String getMetaValue(final String key) {
        if (this.meta.containsKey(key)) {
            return this.meta.get(key);
        }
        return "";
    }

    @JsonIgnore
    public String getResponseHeader(final String key) {
        if (this.responseHeaders.containsKey(key)) {
            return this.responseHeaders.get(key);
        }
        return "";
    }

    public void addResponseHeaders(final Map<String, String> headers) {
        this.responseHeaders.putAll(headers);
    }

    @JsonIgnore
    public void clone(final Request mockRequest) {
        meta.putAll(mockRequest.meta);
        responseHeaders.putAll(mockRequest.responseHeaders);
        body = mockRequest.body;
        name = mockRequest.name;
        url = mockRequest.url;
        processor = mockRequest.processor;
        method = mockRequest.method;
        pathParam.addAll(mockRequest.pathParam);
        queryParam.addAll(mockRequest.queryParam);
        requestQueryParams.putAll(mockRequest.requestQueryParams);
        requestPathParams.putAll(mockRequest.requestPathParams);
        download = mockRequest.download;
        grouping = mockRequest.grouping;
        fileName = mockRequest.fileName;
        validate = mockRequest.validate;
        spec = mockRequest.spec;
    }

    @JsonIgnore
    public String getActURL() {
        if (grouping) {
            int startIndex = -1;
            int i = 0;
            while (i <= 3) {
                startIndex = url.indexOf("/", startIndex + 1);
                i++;
            }
            return url.substring(startIndex);
        }
        return url;

    }
}