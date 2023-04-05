package com.ideas.ngimocker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;
import static java.nio.file.StandardOpenOption.WRITE;

@Component
public class FixtureFileService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

   @Value("${fixture.path}")
   String fixtureDirectory;

   public String readFile(Path filePath) {
        try {
            return readString(filePath, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            logger.error("No Fixtures available at Path:" + path);
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
            logger.error("No Fixtures available at Path:" + path);
            return null;
        }
    }
}
