package com.ideas.anymocker.core.service;

import com.ideas.anymocker.AnyMockerApplication;
import com.ideas.anymocker.core.components.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Service
@Log
public class NewFixtureDownloadService {

    @Autowired
    RequestFactory requestFactory;

    @Autowired
    NewRequestProcessorFactory requestProcessorFactory;
    public void download() {
        try {
            Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (Request request : requestFactory.getRequestList().stream().filter(Request::isDownload).toList()) {
                requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            log.info("Fixture download completed......");
            Files.deleteIfExists(downloading);
            AnyMockerApplication.restart();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }
}
