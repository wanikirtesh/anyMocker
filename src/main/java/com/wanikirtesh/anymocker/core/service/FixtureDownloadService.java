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
            final Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (final Request request : this.requestFactory.getRequestList().stream().filter(Request::isDownload).toList()) {
                FixtureDownloadService.log.info("======= downloading fixture for " + request.getName() + " ==========");
                this.requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            FixtureDownloadService.log.info("####### Fixture download completed ######");
            Files.deleteIfExists(downloading);
            LogWebSocketHandler.broadcast("=========================== Download Finished ========================");
        }catch (final Exception e){
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            FixtureDownloadService.log.severe(sw.toString());
            throw new RuntimeException(e);
        }
    }
    public void download(final String requestName) {
        try {
            final Path downloading = Path.of("./downloadingFixtures");
            Files.createFile(downloading);
            for (final Request request : this.requestFactory.getRequestList().stream().filter(request -> request.getName().equals(requestName)).toList()) {
                FixtureDownloadService.log.info("======= downloading fixture for " + request.getName() + " ==========");
                this.requestProcessorFactory.getProcessor(request.getProcessor()).downloadFixtures(request);
            }
            FixtureDownloadService.log.info("####### Fixture download completed ######");
            Files.deleteIfExists(downloading);
        }catch (final Exception e){
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            FixtureDownloadService.log.severe(sw.toString());
            throw new RuntimeException(e);
        }
    }

    public Map getStats(final String requestName) {
        final Request request = this.requestFactory.getRequest(requestName);
        return this.requestProcessorFactory.getProcessor(request.getProcessor()).getStats(request);
    }
}
