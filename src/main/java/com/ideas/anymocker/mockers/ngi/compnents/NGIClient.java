package com.ideas.anymocker.mockers.ngi.compnents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas.anymocker.core.components.HTTPClient;
import com.ideas.anymocker.core.components.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NGIClient {

     @Autowired
     HTTPClient httpClientWrapper;
    private final Map<String ,String > extParam = new HashMap<>();

    public NGIClient(@Autowired HTTPClient httpClient){
        this.httpClientWrapper = httpClient;
    }

    public List<String> getCorrelationId(NGIProperties ngiProperties) throws JsonProcessingException {
        var content = httpClientWrapper.makeGetRequest(ngiProperties.getHost()+"/statisticsCorrelation/"+ ngiProperties.getClientCode() + "/"
                + ngiProperties.getPropertyCode() + "/" + ngiProperties.getCorrelationID());
        ObjectMapper mapper = new ObjectMapper();
        var stats = mapper.readValue(content, JsonNode.class);
        var startDate = stats.get("lastModifiedDate").toString().substring(1, 11);
        extParam.put("startDate",startDate);
        return stats.findValues("correlationId").stream().map(x -> x.toString().replace("\"","")).collect(Collectors.toList());
    }

    public String processRequest(NGIProperties ngiProperties, Request request, Map<String, String> params) {
        params.put("size",request.getMeta("size"));
        var url = ngiProperties.getHost()+generateURL(request,params);
        return generalize(httpClientWrapper.makeGetRequest(url), ngiProperties);
    }

    private String generalize(String content, NGIProperties props) {
        return content.replaceAll("\"clientCode\" *: *\""+props.getClientCode()+"\"","\"clientCode\" : \"{clientCode}\"").replaceAll("\"propertyCode\" *: *\""+props.getPropertyCode()+"\"","\"propertyCode\" : \"{propertyCode}\"").replace(props.getHost(),"http://mockeserver:9191");
    }

    private String generateURL(Request request, Map<String, String> params) {
        params.putAll(extParam);
        String url = request.getUrl();
        List<String> pathParams =  request.getPathParam();
        List<String> queryParams = request.getQueryParam();
        for (String pathParam : pathParams) {
            url = url.replace("{" + pathParam + "}", params.get(pathParam));
        }
        String queryStr = queryParams.stream().reduce("",(c,k)-> c+= ("&"+k+"="+params.get(k)));
        queryStr = queryStr.length() > 0 ? ("?" + queryStr.substring(1)) : "";
        return url+queryStr;
    }


}
