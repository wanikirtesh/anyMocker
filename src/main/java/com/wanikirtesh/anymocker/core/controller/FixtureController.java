package com.wanikirtesh.anymocker.core.controller;
import com.wanikirtesh.anymocker.AnyMockerApplication;
import com.wanikirtesh.anymocker.core.service.FixtureDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@RestController
public class FixtureController {
    @Autowired
    FixtureDownloadService downloadService;

    @GetMapping(path = "/updateFixtures")
    public ResponseEntity<String> updateFixtures() {
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        CompletableFuture.runAsync(()->
            downloadService.download()
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }

    @GetMapping(path = "/restart")
    public ResponseEntity<String> restartApp(){
        AnyMockerApplication.restart();
        return new ResponseEntity<>("Server restarted",HttpStatus.OK);
    }
}
