package com.ideas.anymocker.mockers.ngi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ideas.anymocker.core.components.Request;
import com.ideas.anymocker.mockers.ngi.compnents.G3Client;
import com.ideas.anymocker.mockers.ngi.compnents.NGIClient;
import com.ideas.anymocker.mockers.ngi.compnents.NGIProperties;
import com.ideas.anymocker.core.service.RequestFactory;
import com.ideas.anymocker.core.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Log
@Service("NGI")
public class NGIRequestProcessor implements RequestProcessor {
    @Value("${fixture.path}")
    String fixturePath;
    @Value("${crawler.path}")
    String crawlerConfigPath;
    @Autowired
    FixtureFileService fixtureFileService;
    @Autowired
    RequestFactory requestFactory;
    @Autowired
    G3Client g3Client;

    @Autowired
    NGIClient ngiClient;
    private static final Map<String, Map<String, Path>> fixturesMap = new HashMap<>();
    private static final Map<String, Map<String, Map<String, Path>>> pagedFixtureMap = new HashMap<>();


    @Override
    @PostConstruct
    public void init()  {
        try {
            log.info("reading fixtures from \"" + fixturePath + "\"");
            long size = getFixtureSize(Path.of(fixturePath));
            log.info("NGI fixture size is:" + ((size/1024)/1024) + "MB");
            requestFactory.getRequests(this).forEach(request -> {
                if (request.isDownload()) {
                    if (request.getMeta("pages").equals("true")) {
                        pagedFixtureMap.put(request.getName(), fixtureFileService.collectNestedFiles(Path.of(fixturePath, request.getName())));
                    } else {
                        fixturesMap.put(request.getName(), fixtureFileService.collectFiles(Path.of(fixturePath, request.getName())));
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private long getFixtureSize(Path path) {
        long size;

        // need close Files.walk
        try (Stream<Path> walk = Files.walk(path)) {

            size = walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            System.out.printf("Failed to get size of %s%n%s", p, e);
                            return 0L;
                        }
                    })
                    .sum();

        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
        return size;
    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        try {
            if(!match.isDownload()){
                return new ResponseEntity<>("success",HttpStatus.OK);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_NDJSON);
            String str_body;
            if (match.getMeta().get("pages").equals("true")) {
               str_body = Files.readString(pagedFixtureMap.get(match.getName()).get(getCorrelationId(match)).get(match.getPage()))
                        .replace("{clientCode}", match.getParameter("clientCode"))
                        .replace("{propertyCode}", match.getParameter("propertyCode"));
                return new ResponseEntity<>(str_body, headers, HttpStatus.OK);
            }
            str_body = Files.readString(fixturesMap.get(match.getName()).get(getCorrelationId(match)))
                    .replace("{clientCode}", match.getParameter("clientCode"))
                    .replace("{propertyCode}", match.getParameter("propertyCode"));
            return new ResponseEntity<>(str_body, headers, HttpStatus.OK);
        }catch (Exception e){
            throw new RuntimeException(("No Fixture Available for request " + match.getUrl() + " query:" + match.getRequestQueryParams()));
        }
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {
        try {
            if (!match.getMeta("g3CallBack").isEmpty()) {
                callBack1(body);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void downloadFixtures(Request match) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<NGIProperties> ngiPropertiesList = mapper.readValue(new File(crawlerConfigPath), new TypeReference<>() {
            });
            for (NGIProperties ngiProperties : ngiPropertiesList) {
                List<String> correlationId = ngiClient.getCorrelationId(ngiProperties);
                downloadFixture(ngiProperties, match, correlationId);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String getCorrelationId(Request match) throws Exception {
        if(match.getRequestQueryParams().containsKey("correlationId")){
            return match.getRequestQueryParams().get("correlationId");
        }
        if(match.getRequestQueryParams().containsKey("statisticsCorrelationId")){
            return match.getRequestQueryParams().get("statisticsCorrelationId");
        }
        if(match.getRequestPathParams().containsKey("correlationId")){
            return match.getRequestPathParams().get("correlationId");
        }
        if(match.getRequestPathParams().containsKey("statisticsCorrelationId")){
            return match.getRequestPathParams().get("statisticsCorrelationId");
        }
        throw new Exception("No Correlation Id Found in request ");
    }


    private void downloadFixture(NGIProperties ngiProperties, Request request, List<String> correlationIds) throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("statisticsCorrelationId", ngiProperties.getCorrelationID());
        params.put("propertyCode", ngiProperties.getPropertyCode());
        params.put("clientCode", ngiProperties.getClientCode());
        params.put("format","haljson");
        if(request.getMeta("correlation").equalsIgnoreCase("true")){
            for (String correlationId : correlationIds) {
                params.put("correlationId",correlationId);
                if(request.getMeta("pages").equals("true")){
                    params.put("page","0");
                    String content = ngiClient.processRequest(ngiProperties,request,params);
                    fixtureFileService.writeFile(content,request.getName(),correlationId,"0.json");
                    int page=1;
                    while (checkNextPageExists(content)){
                        params.put("page",page+"");
                        content = ngiClient.processRequest(ngiProperties,request,params);
                        fixtureFileService.writeFile(content,request.getName(),correlationId,page+".json");
                        page++;
                    }
                }else{
                    String content = ngiClient.processRequest(ngiProperties,request,params);
                    fixtureFileService.writeFile(content,request.getName(),getCorrelationId(request)+".json");
                }
            }
        }else{
            params.put("correlationId", ngiProperties.getCorrelationID());
            if(request.getMeta("pages").equals("true")){
                params.put("page","0");
                String content = ngiClient.processRequest(ngiProperties,request,params);
                fixtureFileService.writeFile(content,request.getName(), ngiProperties.getCorrelationID(),"0.json");
                var page=1;
                while (checkNextPageExists(content)){
                    params.put("page",""+page);
                    content = ngiClient.processRequest(ngiProperties,request,params);
                    fixtureFileService.writeFile(content,request.getName(), ngiProperties.getCorrelationID(),page+".json");
                    page++;
                }
            }else{
                String content = ngiClient.processRequest(ngiProperties,request,params);
                fixtureFileService.writeFile(content,request.getName(), ngiProperties.getCorrelationID()+".json");
            }
        }
    }
    private boolean checkNextPageExists(String page) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readValue(page, JsonNode.class);
        return obj.has("links") && obj.get("links").findValues("rel").contains(new TextNode("next"));
    }

    public void callBack1(Object body) {
        log.info("sending G3 Callback");
        JsonNode nx = new ObjectMapper().valueToTree(body);
        ((ObjectNode) nx).putNull("errors");
        ((ObjectNode) nx).putNull("warnings");
        ((ObjectNode) nx).put("successful", true);
        ((ObjectNode) nx).putObject("response")
                .put("reservationsUpdated", 0)
                .put("groupsUpdated", 0)
                .put("marketSegmentsImpacted", 0);
        log.info("Response is :" + g3Client.sendJobResponse(nx.toString(), nx.get("clientCode").toString().replace("\"", ""), nx.get("propertyCode").toString().replace("\"", ""), "5"));
    }

}
