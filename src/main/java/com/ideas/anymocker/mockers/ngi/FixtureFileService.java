package com.ideas.anymocker.mockers.ngi;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.WRITE;

@Component
@Log
public class FixtureFileService {
    @Value("${fixture.path}")
    String fixtureDirectory;

    public FixtureFileService(String fixtureDirectory){
        this.fixtureDirectory = fixtureDirectory;
    }

    public FixtureFileService(){
        //this.fixtureDirectory = fixtureDirectory;
    }
    public void writeFile(String content, String... paths) throws IOException {
        var filePath = Path.of(fixtureDirectory,paths);
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        Files.write(filePath, content.getBytes(), WRITE);
    }

    public Map<String, Path> collectFiles(Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            this::readFileName,
                            this::readFullFileName)
                    );
        } catch (IOException e) {
            log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }
    private String readFileName(Path filePath) {
        return filePath.getFileName().toString().replaceAll(".json", "");
    }

    private Path readFullFileName(Path filePath){
       return filePath;
    }

    public Map<String, Map<String, Path>> collectNestedFiles(Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            this::readFileName,
                            this::collectFiles)
                    );
        } catch (IOException e) {
            log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }
}
