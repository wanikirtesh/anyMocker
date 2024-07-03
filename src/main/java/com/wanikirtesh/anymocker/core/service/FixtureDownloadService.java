package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.config.LogWebSocketHandler;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
@Service
@Log
public class FixtureDownloadService {
    @Autowired
    RequestFactory requestFactory;
    @Autowired
    RequestProcessorFactory requestProcessorFactory;
    public void download() {
        LogWebSocketHandler.broadcast("=========================== Starting Download ========================");
        try {
            Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (Request request : requestFactory.getRequestList().stream().filter(Request::isDownload).toList()) {
                log.info("======= downloading fixture for " + request.getName() + " ==========");
                requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            log.info("####### Fixture download completed ######");
            Files.deleteIfExists(downloading);
            LogWebSocketHandler.broadcast("=========================== Download Finished ========================");
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.severe(sw.toString());
            throw new RuntimeException(e);
        }
    }
    public void download(String requestName) {
        try {
            Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (Request request : requestFactory.getRequestList().stream().filter(request -> request.getName().equals(requestName)).toList()) {
                log.info("======= downloading fixture for " + request.getName() + " ==========");
                requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            log.info("####### Fixture download completed ######");
            Files.deleteIfExists(downloading);
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.severe(sw.toString());
            throw new RuntimeException(e);
        }
    }

    public Map getStats(String requestName) {
        Request request =  requestFactory.getRequest(requestName);
        return requestProcessorFactory.getProcessor(request.getProcessor()).getStats(request);
    }
}
