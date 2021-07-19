package com.ideas.ngimocker.controller;
import com.ideas.ngimocker.service.CrawlerService;
import com.ideas.ngimocker.service.RequestMatcherService;
import com.ideas.ngimocker.service.MockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class UniversalMockController {

    @Autowired
    MockerService mockService;

    @Autowired
    RequestMatcherService requestMatcherService;

    @Autowired
    CrawlerService crawlerService;

    @GetMapping(path = "/updateFixtures")
    public ResponseEntity<String> updateFixtures() throws Exception {
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        crawlerService.fetchFixtures();
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }

    @RequestMapping(path = "/**",produces ="application/json")
    public ResponseEntity<String> defaultRequest(HttpServletRequest req,@RequestParam Map<String,String> queryParams,@RequestBody(required = false) Object body) throws Exception {
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }else {
            return mockService.processRequest(requestMatcherService.match(req,queryParams,body),body);
        }

    }
}
