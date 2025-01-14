package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.GroovyHelper;
import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class FixtureController {
    @Autowired
    private FixtureDownloadService downloadService;
    @Autowired
    private MockerService mockerService;
    @Autowired
    private RequestFactory requestFactory;
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private RequestMatcherService requestMatcherService;

    @Value("${processors.path}")
    private String processorsPath;

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOAD/{requestName}")
    public ResponseEntity<String> downloadSpecific(@PathVariable final String requestName) {
        if (Files.exists(Path.of("./downloadingFixtures"))) {
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }
        CompletableFuture.runAsync(() ->
        {this.downloadService.download(requestName);
                this.mockerService.reload(requestFactory.getRequest(requestName).getProcessor());}
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOADALL")
    public ResponseEntity<String> downloadAll() {
        if (Files.exists(Path.of("./downloadingFixtures"))) {
            return new ResponseEntity<>("Server is downloading Fixtures.... ", HttpStatus.OK);
        }
        CompletableFuture.runAsync(() ->
                this.downloadService.download()
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/STATS/{requestName}")
    public ResponseEntity<Map> getSpecific(@PathVariable final String requestName) {
        return new ResponseEntity<>(this.downloadService.getStats(requestName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/RELOAD/{requestName}")
    public ResponseEntity<String> reload(@PathVariable final String requestName) throws IOException {
        this.requestProcessorFactory.updateProcessor(this.requestFactory.getRequest(requestName).getProcessor());
        this.mockerService.reload(this.requestFactory.getRequest(requestName).getProcessor());
        return new ResponseEntity<>("reloaded", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/STORAGE")
    public ResponseEntity<Map> getStorage() {
        return new ResponseEntity<>(GroovyHelper.getStorage(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/SAVEPROCESSOR/{file}", consumes = "text/plain")
    public ResponseEntity<String> getProcessor(@PathVariable final String file, @RequestBody final String content) throws IOException {
        Path path = Paths.get(this.processorsPath, file + ".groovy");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        this.requestProcessorFactory.updateProcessor(file);
        this.mockerService.reload(file);
        return new ResponseEntity<>("Saved file", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/MOCKER/MANAGE/REQUEST/V2/SAVE")
    public ResponseEntity<String> saveRequestv2(@RequestParam String fileName, @RequestBody Request request) throws IOException {
        request.setFileName(fileName);
        final Request existingRequest = requestMatcherService.isMatchedAny(request);
        if(null == existingRequest) {
                requestFactory.saveRequest(request);
            return new ResponseEntity<>("success", HttpStatus.OK);
        }else{
            log.warn("API already exist ("+existingRequest.getUrl()+") "+existingRequest.getName()+" in module " + existingRequest.getFileName());
            return new ResponseEntity<>("API already exist ("+existingRequest.getMethod()+") in module " + existingRequest.getFileName(),HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/MOCKER/MANAGE/REQUEST/V2/DELETE/{requestName}")
    public ResponseEntity<String> deleteRequestv2(@RequestParam String fileName, @PathVariable String requestName) throws IOException {
        requestFactory.deleteRequest(requestName, fileName);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/REQUEST/SAVE/{requestName}")
    public ResponseEntity<Request> saveRequest(@PathVariable String requestName,@RequestParam String fileName ,@RequestBody Request request) throws IOException {
         request.setFileName(fileName);
         requestFactory.saveRequest(request, requestName);
         return new ResponseEntity<>(request, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/RELOAD")
    public ResponseEntity<String> reload() throws IOException {
        requestFactory.reload();
        mockerService.reload();
        return new ResponseEntity<>("reloaded", HttpStatus.OK);
    }
}
