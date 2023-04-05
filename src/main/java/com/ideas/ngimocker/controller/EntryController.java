package com.ideas.ngimocker.controller;

import com.ideas.ngimocker.service.MockerService;
import com.ideas.ngimocker.service.RequestMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping(path = "/**", consumes = "*/*", produces = "*/*")

public class EntryController {
    @Autowired
    MockerService mockService;

    @Autowired
    RequestMatcherService requestMatcherService;

    @RequestMapping
    public ResponseEntity<String> handleRequest(HttpServletRequest req, @RequestParam(required = false) Map<String,String> queryParams, @RequestBody(required = false) String body) throws Exception {
        // Handle the request here
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }else {
            return mockService.processRequest(requestMatcherService.match(req,queryParams,body),body,req);
        }
    }
}
