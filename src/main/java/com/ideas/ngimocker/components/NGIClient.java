package com.ideas.ngimocker.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NGIClient {

    private final String STAT_CORRELATION = "/statisticsCorrelation/";

    @Autowired
    HTTPClientWrapper httpClientWrapper;
    private Map<String ,String > extParam = new HashMap<>();

    public List<String> getCorrelationId(NGIProps ngiProps) throws JsonProcessingException {
        var content = httpClientWrapper.makeGetRequest(ngiProps.getHost()+STAT_CORRELATION+ ngiProps.getClientCode() + "/"
                + ngiProps.getPropertyCode() + "/" + ngiProps.getCorrelationID());
        ObjectMapper mapper = new ObjectMapper();
        var stats = mapper.readValue(content, JsonNode.class);
        var startDate = stats.get("lastModifiedDate").toString().substring(1, 11);
        extParam.put("startDate",startDate);
        return stats.findValues("correlationId").stream().map(x -> x.toString().replace("\"","")).collect(Collectors.toList());
    }

    public String processRequest(NGIProps ngiProps, MockRequest request, Map<String, String> params) {
        params.put("size",request.getSize()+"");
        var url = ngiProps.getHost()+generateURL(request,params);
        return generalize(httpClientWrapper.makeGetRequest(url),ngiProps);
    }

    private String generalize(String content,NGIProps props) {
        return content.replace("\"clientCode\" : \""+props.getClientCode()+"\"","\"clientCode\" : \"{clientCode}\"").replace("\"propertyCode\" : \""+props.getClientCode()+"\"","\"propertyCode\" : \"{propertyCode}\"").replace(props.getHost(),"http://mockeserver:9191");
    }

    private String generateURL(MockRequest request, Map<String, String> params) {
        params.putAll(extParam);
        String url = request.getUrl();
        List<String> pathParams =  request.getPathParam();
        List<String> queryParams = request.getQueryParam();
        for(int i=0;i<pathParams.size();i++){
            url = url.replace("{" + pathParams.get(i) + "}", params.get(pathParams.get(i)));
        }
        String queryStr = "";
        for(int i=0;i<queryParams.size();i++){
            queryStr += ("&"+queryParams.get(i) + "=" + params.get(queryParams.get(i)));
        }
        queryStr = queryStr.length()>0?("?"+queryStr.substring(1)):"";
        return url+queryStr;
    }


}
