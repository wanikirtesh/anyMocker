package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.GroovyHelper;
import com.wanikirtesh.anymocker.core.components.OpenApiImporter;
import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
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
    @Value("${specs.path}")
    private String specPath;

    @Value("${processors.path}")
    private String processorsPath;

    private final String initProc="import com.wanikirtesh.anymocker.core.components.GroovyHelper\n" +
            "/*\n" +
            "void putObject(String key,Object o)\n" +
            "Object getDataObject(String key)\n" +
            "ResponseEntity<String> getResponseEntity(String body, Map<String,String> headers, int statusCode)\n" +
            "ResponseEntity<String> getResponseEntity(String body, int statusCode)\n" +
            "ResponseEntity<String> getResponseEntity(int statusCode)\n" +
            "HttpResponse<String> makePostRequest(String uri, String data, String[] headers)\n" +
            "String makeGetRequest(String uri)\n" +
            "String makeRequest(String uri, String method, HttpRequest.BodyPublisher body)\n" +
            "void writeFile(String content,String basePath, String... paths)\n" +
            "Map<String, Path> collectFiles(Path path)\n" +
            "Map<String, Map<String, Path>> collectNestedFiles(Path path)\n" +
            "JSONObject parseJsonObject(String str)\n" +
            "JSONArray parseJsonArray(String str)\n" +
            "*/\n" +
            "\n" +
            "def init(log,requests){\n" +
            "\n" +
            "}\n" +
            "\n" +
            "def process(log,match,body,req){\n" +
            "    return GroovyHelper.getResponseEntity(\"success\",200)\n" +
            "}\n" +
            "\n" +
            "def post( log,match, body, req){\n" +
            "    \n" +
            "}\n" +
            "def pre( mlog,match, body, req){\n" +
            "    \n" +
            "}\n" +
            "\n" +
            "def download(log,match){\n" +
            "\n" +
            "}\n" +
            "\n" +
            "def stats(log,match){\n" +
            "\n" +
            "}\n" +
            "\n" +
            "\n";

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
    public ResponseEntity<String> saveProcessor(@PathVariable final String file, @RequestBody final String content) throws IOException {
        Path path = Paths.get(this.processorsPath, file + ".groovy");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        this.requestProcessorFactory.updateProcessor(file);
        this.mockerService.reload(file);
        return new ResponseEntity<>("Saved file", HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PATCH, path = "/MOCKER/MANAGE/SAVESPEC/{file}", consumes = "text/plain")
    public ResponseEntity<String> saveSpec(@PathVariable final String file, @RequestBody final String content) throws IOException {
        Path path = Paths.get(this.specPath, file);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return new ResponseEntity<>("Saved file", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/MOCKER/MANAGE/REQUEST/V2/SAVE")
    public ResponseEntity<String> saveRequestv2(@RequestParam String fileName, @RequestBody Request request) throws IOException {
        request.setFileName(fileName);
        final Request existingRequest = requestMatcherService.isMatchedAny(request);
        if(null == existingRequest) {
                requestFactory.saveRequest(request);
                createProcessorIfNotExist(request);
            return new ResponseEntity<>("success", HttpStatus.OK);
        }else{
            log.warn("API already exist ("+existingRequest.getUrl()+") "+existingRequest.getName()+" in module " + existingRequest.getFileName());
            return new ResponseEntity<>("API already exist ("+existingRequest.getMethod()+") in module " + existingRequest.getFileName(),HttpStatus.CONFLICT);
        }
    }

    private void createProcessorIfNotExist(Request request) throws IOException {
        try {
            Path processorPath = Paths.get(processorsPath, request.getProcessor() + ".groovy");
            File processorFile = processorPath.toFile();
            if (!processorFile.exists()) {
                Files.write(processorPath, initProc.getBytes());
            }
        }catch(Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/MOCKER/MANAGE/REQUEST/V2/DELETE/{requestName}")
    public ResponseEntity<String> deleteRequestv2(@RequestParam String fileName, @PathVariable String requestName) throws IOException {
        requestFactory.deleteRequest(requestName, fileName);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT,path = "/MOCKER/MANAGE/IMPORT/{moduleName}")
    public ResponseEntity<String> importModule(@PathVariable String moduleName, @RequestBody final String requestYml) throws IOException {
        try{
            List<Request> requests = OpenApiImporter.importFromOpenApiSpec(requestYml);
            String specFileName = "spec_" + (new Date().getTime()) + ".yaml";
            Files.write(Paths.get(specPath,specFileName), requestYml.getBytes());
            List<Request> list = requests.stream().peek(req -> {req.setValidate(true);req.setSpec(specFileName);}).toList();
            requestFactory.saveRequests(moduleName,list);
            return new ResponseEntity<>("success", HttpStatus.OK);
        }catch(Exception e){
            log.error(e.getMessage());
            throw e;
        }
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
