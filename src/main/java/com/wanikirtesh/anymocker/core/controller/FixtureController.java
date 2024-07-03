package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.service.FixtureDownloadService;
import com.wanikirtesh.anymocker.core.service.MockerService;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import com.wanikirtesh.anymocker.core.service.RequestProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class FixtureController {
    @Autowired
    private FixtureDownloadService downloadService;
    @Autowired
    private MockerService mockerService;
    @Autowired
    private RequestFactory requestFactory;
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

   @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOAD/{requestName}")
    public ResponseEntity<String> downloadSpecific(@PathVariable String requestName){
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        CompletableFuture.runAsync(()->
                downloadService.download(requestName)
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOADALL")
    public ResponseEntity<String> downloadAll(){
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        CompletableFuture.runAsync(()->
             downloadService.download()
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/STATS/{requestName}")
    public ResponseEntity<Map> getSpecific(@PathVariable String requestName){
        return new ResponseEntity<>(downloadService.getStats(requestName),HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/RELOAD/{requestName}")
    public ResponseEntity<String> reload(@PathVariable String requestName) throws IOException {
        requestFactory.reload();
        requestProcessorFactory.updateProcessor(requestFactory.getRequest(requestName).getProcessor());
        mockerService.reload();
        return new ResponseEntity<>("reloaded",HttpStatus.OK);
    }
}
