package com.ideas.anymocker.core.controller;

import com.ideas.anymocker.core.service.MockerService;
import com.ideas.anymocker.core.service.NewMockerService;
import com.ideas.anymocker.core.service.RequestMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping(path = "/**", consumes = "*/*", produces = "*/*")
public class EntryController {
    @Autowired
    private MockerService mockService;

    @Value("${use.groovy.impl:true}")
    private boolean useGroovy;

    @Autowired
    private NewMockerService newMockerService;
    @Autowired
    private RequestMatcherService requestMatcherService;

    @RequestMapping
    public ResponseEntity<String> handleRequest(HttpServletRequest req, @RequestParam(required = false) Map<String,String> queryParams, @RequestBody(required = false) String body) {
        // Handle the request here
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }else {
            if(useGroovy)
                return newMockerService.processRequest(requestMatcherService.match(req,queryParams,body),body,req);
            else
                return mockService.processRequest(requestMatcherService.match(req,queryParams,body),body,req);

        }
    }


}
