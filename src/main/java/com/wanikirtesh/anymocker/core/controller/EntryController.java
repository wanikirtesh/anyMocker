package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.service.MockerService;
import com.wanikirtesh.anymocker.core.service.RequestMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping(path = "/**", consumes = "*/*", produces = "*/*")
public class EntryController {

    @Autowired
    private MockerService mockerService;
    @Autowired
    private RequestMatcherService requestMatcherService;

    @RequestMapping
    public ResponseEntity<String> handleRequest(HttpServletRequest req, @RequestParam(required = false) Map<String,String> queryParams, @RequestBody(required = false) String body) {
        // Handle the request here
        if(Files.exists(Paths.get("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }else {
            //if(useGroovy)
                return mockerService.processRequest(requestMatcherService.match(req,queryParams,body),body,req);
            //else
            //    return mockService.processRequest(requestMatcherService.match(req,queryParams,body),body,req);

        }
    }


}
