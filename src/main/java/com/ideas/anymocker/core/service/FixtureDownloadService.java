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
public class FixtureDownloadService {

    @Autowired
    RequestFactory requestFactory;

    @Autowired
    RequestProcessorFactory requestProcessorFactory;
    public void download() {
        try {
            Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (Request request : requestFactory.getRequestList().stream().filter(Request::isDownload).collect(Collectors.toList())) {
                requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            log.info("Fixture download Completed......");
            Files.deleteIfExists(downloading);
            AnyMockerApplication.restart();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }
}
