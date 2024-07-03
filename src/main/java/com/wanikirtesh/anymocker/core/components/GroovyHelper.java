package com.wanikirtesh.anymocker.core.components;

import com.wanikirtesh.anymocker.core.config.LogWebSocketHandler;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.net.http.HttpRequest.newBuilder;
import static java.nio.file.StandardOpenOption.WRITE;

@Log
public class GroovyHelper {
    private static final Map<String,Object> store = new ConcurrentHashMap<>();
    public static void putObject(String key,Object o){
        store.put(key,o);
    }
    public static Object getDataObject(String key){
        return store.get(key);
    }
    public static ResponseEntity<String> getResponseEntity(String body, Map<String,String> headers, int statusCode){
        HttpHeaders actHeaders = new HttpHeaders();
        for(String headerName:headers.keySet()){
         actHeaders.set(headerName,headers.get(headerName));
        }
        return new ResponseEntity<>(body,actHeaders,statusCode);
    }
    public static ResponseEntity<String> getResponseEntity(String body, int statusCode){
        HttpHeaders actHeaders = new HttpHeaders();
        return new ResponseEntity<>(body,actHeaders,statusCode);
    }

    public static ResponseEntity<String> getResponseEntity(int statusCode){
        HttpHeaders actHeaders = new HttpHeaders();
        return new ResponseEntity<>("",actHeaders,statusCode);
    }

    public static HttpResponse<String> makePostRequest(String uri, String data, String[] headers) {
        log.info("Sending post request to :" + uri);
        log.finer("with Body " + data);
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers(headers)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("Response code for the request is:" + response.statusCode());
        return response;
    }

    public static String makeGetRequest(String uri) {
        return makeRequest(uri, "GET", HttpRequest.BodyPublishers.noBody());
    }
    private static String makeRequest(String uri, String method, HttpRequest.BodyPublisher body) {
        LogWebSocketHandler.broadcast("Making request " + uri);
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            log.info("Request " + method + " " + uri);
            HttpResponse<String> response = httpClient.send(
                    newBuilder(URI.create(uri)).method(method, body).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                String msg = "Unrecognized response from server. " + response.statusCode() + " for url " + uri;
                throw new RuntimeException(msg);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void writeFile(String content,String basePath, String... paths) {
        try {
            var filePath = Path.of(basePath, paths);
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            Files.write(filePath, content.getBytes(), WRITE);
        }catch (Exception e){
            log.severe("error while writing file "+e.getMessage());
            e.printStackTrace();
            throw(new RuntimeException(e));
        }
    }
    public static  Map<String, Path> collectFiles(Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            GroovyHelper::readFileName,
                            GroovyHelper::readFullFileName)
                    );
        } catch (IOException e) {
            log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }
    private static String readFileName(Path filePath) {
        return filePath.getFileName().toString().replaceAll(".json", "");
    }
    private static Path readFullFileName(Path filePath){
        return filePath;
    }

    public static Map<String, Map<String, Path>> collectNestedFiles(Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            GroovyHelper::readFileName,
                            GroovyHelper::collectFiles)
                    );
        } catch (IOException e) {
            log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }

    public static JSONObject parseJsonObject(String str){
        return new JSONObject(str);
    }

    public static JSONArray parseJsonArray(String str){
        return new JSONArray(str);
    }


}
