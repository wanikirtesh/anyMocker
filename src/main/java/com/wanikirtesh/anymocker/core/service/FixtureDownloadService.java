package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.AnyMockerApplication;
import com.wanikirtesh.anymocker.core.components.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Log
public class FixtureDownloadService {
    @Autowired
    RequestFactory requestFactory;

    @Autowired
    RequestProcessorFactory requestProcessorFactory;
    public void download() {
        try {
            Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (Request request : requestFactory.getRequestList().stream().filter(Request::isDownload).toList()) {
                log.info("downloading fixture for " + request.getName());
                requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            log.info("Fixture download completed......");
            Files.deleteIfExists(downloading);
            AnyMockerApplication.restart();
        }catch (Exception e){
            log.severe(e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.severe(sw.toString());
            throw new RuntimeException(e);
        }


    }
}
