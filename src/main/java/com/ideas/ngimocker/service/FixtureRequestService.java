package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.components.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


@Service
public class FixtureRequestService implements RequestProcessor {

    @Value("${fixture.path}")
    String fixturePath;

    @Autowired
    FixtureFileService fixtureFileService;

    @Autowired
    MockRequestMapper mockRequestMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Map<String, Path>> fixturesMap = new HashMap<>();
    private final Map<String, Map<String, Map<String, Path>>> pagedFixtureMap = new HashMap<String, Map<String, Map<String, Path>>>();

    @PostConstruct
    public void init()  {
        logger.info("reading fixtures from \"" + fixturePath + "\"");
        mockRequestMapper.getRequestList().forEach(key-> {
            if(key.getStore()== RequestHandler.FIXTURE) {
                if (key.isPages()) {
                    pagedFixtureMap.put(key.getLabel(), fixtureFileService.collectNestedFiles(Path.of(fixturePath, key.getLabel())));
                } else {
                    fixturesMap.put(key.getLabel(), fixtureFileService.collectFiles(Path.of(fixturePath, key.getLabel())));
                }
            }
        });
    }



      @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_NDJSON);
            String str_body = "";
            if (match.isPages()) {
               str_body = Files.readString(pagedFixtureMap.get(match.getLabel()).get(match.getRequestedCorrelationId()).get(match.getPage()))
                        .replace("{clientCode}", match.getClientCode())
                        .replace("{propertyCode}", match.getPropertyCode());
                return new ResponseEntity<String>(str_body, headers, HttpStatus.OK);
            }
            str_body = Files.readString(fixturesMap.get(match.getLabel()).get(match.getRequestedCorrelationId()))
                    .replace("{clientCode}", match.getClientCode())
                    .replace("{propertyCode}", match.getPropertyCode());
            return new ResponseEntity<String>(str_body, headers, HttpStatus.OK);
        }catch (Exception e){
            throw new RuntimeException(("No Fixture Available for request " + match.getUrl() + " query:" + match.getRequestQueryParams()));
        }
    }
}
