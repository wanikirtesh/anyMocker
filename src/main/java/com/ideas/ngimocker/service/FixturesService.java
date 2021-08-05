package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


@Component
public class FixturesService {

    @Value("${fixture.path}")
    String fixturePath;

    @Autowired
    FileService fileService;

    @Autowired
    MockRequestService mockRequestService;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Map<String,String>> fixturesMap = new HashMap<>();
    private final Map<String,Map<String,Map<String,String>>> pagedFixtureMap = new HashMap<>();

    @PostConstruct
    public void init()  {
        logger.info("reading fixtures from \"" + fixturePath + "\"");
        mockRequestService.getRequestList().forEach(key-> {
            if(key.isStore()) {
                if (key.isPages()) {
                    pagedFixtureMap.put(key.getLabel(), fileService.collectNestedFiles(Path.of(fixturePath, key.getLabel())));
                } else {
                    fixturesMap.put(key.getLabel(), fileService.collectFiles(Path.of(fixturePath, key.getLabel())));
                }
            }
        });
    }



    public String getFixtures(MockRequest match) throws Exception {
        try {
            if (match.isPages()) {
                return pagedFixtureMap.get(match.getLabel()).get(match.getRequestedCorrelationId()).get(match.getPage())
                        .replace("{clientCode}", match.getClientCode())
                        .replace("{propertyCode}", match.getPropertyCode());
            }
            return fixturesMap.get(match.getLabel()).get(match.getRequestedCorrelationId())
                    .replace("{clientCode}", match.getClientCode())
                    .replace("{propertyCode}", match.getPropertyCode());
        }catch (NullPointerException e){
            throw new Exception("No Fixture Available for request " + match.getUrl() + " query:" + match.getRequestQueryParams());
        }
    }
}
