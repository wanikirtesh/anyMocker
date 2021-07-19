package com.ideas.ngimocker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ideas.ngimocker.NgimockerApplication;
import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.components.NGIClient;
import com.ideas.ngimocker.components.NGIProps;
import com.ideas.ngimocker.components.PathList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class CrawlerService {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PathList pathList;

    @Autowired
    NGIClient ngiClient;

    @Value("${crawler.path}")
    String crawlerConfigPath;

    @Autowired
    FileStoreService fileStoreService;

    @Async
    public void fetchFixtures() throws Exception {
        Files.createFile(Path.of("./downloadingFixtures"));
        ObjectMapper mapper = new ObjectMapper();
        List<NGIProps> ngiPropsList = mapper.readValue(new File(crawlerConfigPath), new TypeReference<>() {});
        for (NGIProps ngiProps : ngiPropsList) {
            List<String> correlationId = ngiClient.getCorrelationId(ngiProps);
            for (MockRequest mockRequest : pathList.get().stream().filter(x-> x.isStore()).collect(Collectors.toList())) {
                downloadFixture(ngiProps, mockRequest, correlationId);
            }
        }
        logger.info("Fixture download Completed......");
        Files.deleteIfExists(Path.of("./downloadingFixtures"));
        NgimockerApplication.restart();
    }

    private void downloadFixture(NGIProps ngiProps, MockRequest request, List<String> correlationIds) throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("statisticsCorrelationId",ngiProps.getCorrelationID());
        params.put("propertyCode",ngiProps.getPropertyCode());
        params.put("clientCode",ngiProps.getClientCode());
        params.put("format","haljson");
        if(request.isCorrelation()){
            for (String correlationId : correlationIds) {
                params.put("correlationId",correlationId);
                if(request.isPages()){
                    params.put("page","0");
                    String content = ngiClient.processRequest(ngiProps,request,params);
                    fileStoreService.writeFile(content,request.getLabel(),correlationId,"0.json");
                    var page=1;
                    while (checkNextPageExists(content)){
                        params.put("page",""+page);
                        content = ngiClient.processRequest(ngiProps,request,params);
                        fileStoreService.writeFile(content,request.getLabel(),correlationId,page+".json");
                        page++;
                    }
                }else{
                    String content = ngiClient.processRequest(ngiProps,request,params);
                    fileStoreService.writeFile(content,request.getLabel(),request.getRequestedCorrelationId()+".json");
                }
            }
        }else{
            params.put("correlationId",ngiProps.getCorrelationID());
            if(request.isPages()){
                params.put("page","0");
                String content = ngiClient.processRequest(ngiProps,request,params);
                fileStoreService.writeFile(content,request.getLabel(),ngiProps.getCorrelationID(),"0.json");
                var page=1;
                while (checkNextPageExists(content)){
                    params.put("page",""+page);
                    content = ngiClient.processRequest(ngiProps,request,params);
                    fileStoreService.writeFile(content,request.getLabel(),ngiProps.getCorrelationID(),page+".json");
                    page++;
                }
            }else{
                String content = ngiClient.processRequest(ngiProps,request,params);
                fileStoreService.writeFile(content,request.getLabel(),ngiProps.getCorrelationID()+".json");
            }
        }
    }

    private boolean checkNextPageExists(String page) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readValue(page, JsonNode.class);
        return obj.has("links") && obj.get("links").findValues("rel").contains(new TextNode("next"));
    }
}
