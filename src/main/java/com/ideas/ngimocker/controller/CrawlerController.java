package com.ideas.ngimocker.controller;
import com.ideas.ngimocker.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class CrawlerController {

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


}
