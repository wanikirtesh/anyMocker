package com.ideas.ngimocker.service.processor;

import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.service.FixtureFileService;
import com.ideas.ngimocker.service.G3CallbackService;
import com.ideas.ngimocker.service.MockRequestMapper;
import com.ideas.ngimocker.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Log
@Service("NGI")
public class NGIRequestProcessor implements RequestProcessor {
    @Value("${fixture.path}")
    String fixturePath;
    @Autowired
    FixtureFileService fixtureFileService;
    @Autowired
    MockRequestMapper mockRequestMapper;
    @Autowired
    G3CallbackService g3CallbackService;
    private static final Map<String, Map<String, Path>> fixturesMap = new HashMap<>();
    private static final Map<String, Map<String, Map<String, Path>>> pagedFixtureMap = new HashMap<String, Map<String, Map<String, Path>>>();

    @PostConstruct
    public void init()  {
        log.info("reading fixtures from \"" + fixturePath + "\"");
        mockRequestMapper.getRequestList().forEach(key-> {
            if(key.getProcessor().equals("NGI") && key.isDownload()) {
                if (key.getMeta("pages").equals("true")) {
                    pagedFixtureMap.put(key.getName(), fixtureFileService.collectNestedFiles(Path.of(fixturePath, key.getName())));
                } else {
                    fixturesMap.put(key.getName(), fixtureFileService.collectFiles(Path.of(fixturePath, key.getName())));
                }
            }
        });
    }

    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        try {
            if(!match.isDownload()){
                return new ResponseEntity<>("success",HttpStatus.OK);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_NDJSON);
            String str_body = "";
            if (match.getMeta().get("pages").equals("true")) {
               str_body = Files.readString(pagedFixtureMap.get(match.getName()).get(getCorrelationId(match)).get(match.getPage()))
                        .replace("{clientCode}", match.getParameter("clientCode"))
                        .replace("{propertyCode}", match.getParameter("propertyCode"));
                return new ResponseEntity<String>(str_body, headers, HttpStatus.OK);
            }
            str_body = Files.readString(fixturesMap.get(match.getName()).get(getCorrelationId(match)))
                    .replace("{clientCode}", match.getParameter("clientCode"))
                    .replace("{propertyCode}", match.getParameter("propertyCode"));
            return new ResponseEntity<String>(str_body, headers, HttpStatus.OK);
        }catch (Exception e){
            throw new RuntimeException(("No Fixture Available for request " + match.getUrl() + " query:" + match.getRequestQueryParams()));
        }
    }

    @Override
    public void postProcess(MockRequest match, String body, HttpServletRequest req) {
        try {
            if (!match.getMeta("g3CallBack").isEmpty()) {
                g3CallbackService.callBack1(body);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void preProcess(MockRequest match, String body, HttpServletRequest req) {

    }

    private String getCorrelationId(MockRequest match) throws Exception {
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
}
