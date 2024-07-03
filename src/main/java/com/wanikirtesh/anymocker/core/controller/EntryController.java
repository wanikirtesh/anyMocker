package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.service.MockerService;
import com.wanikirtesh.anymocker.core.service.RequestMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(headers = "!Upgrade")
    public ResponseEntity<String> handleRequest(final HttpServletRequest req, @RequestParam(required = false) final Map<String,String> queryParams, @RequestBody(required = false) final String body) {
        if(Files.exists(Paths.get("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }else {
            return this.mockerService.processRequest(this.requestMatcherService.match(req,queryParams,body),body,req);
        }
    }
}
