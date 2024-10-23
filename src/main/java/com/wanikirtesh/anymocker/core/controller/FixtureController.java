package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.GroovyHelper;
import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.FixtureDownloadService;
import com.wanikirtesh.anymocker.core.service.MockerService;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import com.wanikirtesh.anymocker.core.service.RequestProcessorFactory;
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
public class FixtureController {
    @Autowired
    private FixtureDownloadService downloadService;
    @Autowired
    private MockerService mockerService;
    @Autowired
    private RequestFactory requestFactory;
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Value("${processors.path}")
    private String processorsPath;

   @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOAD/{requestName}")
    public ResponseEntity<String> downloadSpecific(@PathVariable final String requestName){
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        CompletableFuture.runAsync(()->
                this.downloadService.download(requestName)
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/DOWNLOADALL")
    public ResponseEntity<String> downloadAll(){
        if(Files.exists(Path.of("./downloadingFixtures"))){
            return new ResponseEntity<>("Server is downloading Fixtures.... ",HttpStatus.OK);
        }
        CompletableFuture.runAsync(()->
                this.downloadService.download()
        );
        return new ResponseEntity<>("Server will restart after Downloading fixture",HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/STATS/{requestName}")
    public ResponseEntity<Map> getSpecific(@PathVariable final String requestName){
        return new ResponseEntity<>(this.downloadService.getStats(requestName),HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/RELOAD/{requestName}")
    public ResponseEntity<String> reload(@PathVariable final String requestName) throws IOException {
        // this.requestFactory.reload();
        this.requestProcessorFactory.updateProcessor(this.requestFactory.getRequest(requestName).getProcessor());
        this.mockerService.reload(this.requestFactory.getRequest(requestName).getProcessor());
        return new ResponseEntity<>("reloaded",HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/STORAGE")
    public ResponseEntity<Map> getStorage(){
        return new ResponseEntity<>(GroovyHelper.getStorage(),HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/MOCKER/MANAGE/PROCESSOR/{file}")
    public ResponseEntity<String> getProcessor(@PathVariable final String file) throws IOException {
        final String content = Files.readString(Paths.get(this.processorsPath, file + ".groovy"));
        return new ResponseEntity<>(content,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/SAVEPROCESSOR/{file}",consumes = "text/plain")
    public ResponseEntity<String> getProcessor(@PathVariable final String file, @RequestBody final String content) throws IOException {
        Path path = Paths.get(this.processorsPath, file + ".groovy");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path,content.getBytes(StandardCharsets.UTF_8));
        this.requestProcessorFactory.updateProcessor(file);
        this.mockerService.reload(file);
        return new ResponseEntity<>("Saved file",HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/REQUEST/GET/{request}")
    public ResponseEntity<Request> getRequest(@PathVariable final String request) throws IOException {
        return new ResponseEntity<>(requestFactory.getRequest(request),HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.POST, path = "/MOCKER/MANAGE/REQUEST/SAVE")
    public ResponseEntity<Request> saveRequest(@RequestBody Request request) throws IOException {
       requestFactory.saveRequest(request);
       return new ResponseEntity<>(request,HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.POST, path = "/MOCKER/MANAGE/REQUEST/SAVE/{requestName}")
    public ResponseEntity<Request> saveRequest(@PathVariable String requestName, @RequestBody Request request) throws IOException {
        requestFactory.saveRequest(request,requestName);
        return new ResponseEntity<>(request,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET,path = "/MOCKER/MANAGE/RELOAD")
    public ResponseEntity<String> reload() throws IOException {
       requestFactory.reload();
       mockerService.reload();
       return new ResponseEntity<>("reloaded",HttpStatus.OK);
    }
}
