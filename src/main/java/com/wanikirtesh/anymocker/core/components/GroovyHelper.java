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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.net.http.HttpRequest.newBuilder;
import static java.nio.file.StandardOpenOption.WRITE;

@Log
public enum GroovyHelper {
    ;
    private static final Map<String,Object> store = new ConcurrentHashMap<>();
    public static void putObject(final String key, final Object o){
        GroovyHelper.store.put(key,o);
    }
    public static Object getDataObject(final String key){
        return GroovyHelper.store.get(key);
    }
    public static ResponseEntity<String> getResponseEntity(final String body, final Map<String,String> headers, final int statusCode){
        final HttpHeaders actHeaders = new HttpHeaders();
        for(final String headerName:headers.keySet()){
         actHeaders.set(headerName,headers.get(headerName));
        }
        return new ResponseEntity<>(body,actHeaders,statusCode);
    }
    public static ResponseEntity<String> getResponseEntity(final String body, final int statusCode){
        final HttpHeaders actHeaders = new HttpHeaders();
        return new ResponseEntity<>(body,actHeaders,statusCode);
    }

    public static ResponseEntity<String> getResponseEntity(final int statusCode){
        final HttpHeaders actHeaders = new HttpHeaders();
        return new ResponseEntity<>("",actHeaders,statusCode);
    }

    public static HttpResponse<String> makePostRequest(final String uri, final String data, final String[] headers) {
        GroovyHelper.log.info("Sending post request to :" + uri);
        GroovyHelper.log.finer("with Body " + data);
        final HttpClient client = HttpClient.newBuilder().build();
        final HttpRequest request = newBuilder()
                .uri(URI.create(uri))
                .headers(headers)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            GroovyHelper.log.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        GroovyHelper.log.info("Response code for the request is:" + response.statusCode());
        return response;
    }

    public static String makeGetRequest(final String uri) {
        return GroovyHelper.makeRequest(uri, "GET", HttpRequest.BodyPublishers.noBody());
    }
    private static String makeRequest(final String uri, final String method, final HttpRequest.BodyPublisher body) {
        LogWebSocketHandler.broadcast("Making request " + uri);
        try {
            final HttpClient httpClient = HttpClient.newHttpClient();
            GroovyHelper.log.info("Request " + method + " " + uri);
            final HttpResponse<String> response = httpClient.send(
                    newBuilder(URI.create(uri)).method(method, body).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (400 <= response.statusCode()) {
                final String msg = "Unrecognized response from server. " + response.statusCode() + " for url " + uri;
                throw new RuntimeException(msg);
            }
            return response.body();
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(final String content, final String basePath, final String... paths) {
        try {
            final var filePath = Path.of(basePath, paths);
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), WRITE);
        }catch (final Exception e){
            GroovyHelper.log.severe("error while writing file "+e.getMessage());
            e.printStackTrace();
            throw(new RuntimeException(e));
        }
    }
    public static  Map<String, Path> collectFiles(final Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            GroovyHelper::readFileName,
                            GroovyHelper::readFullFileName)
                    );
        } catch (final IOException e) {
            GroovyHelper.log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }
    private static String readFileName(final Path filePath) {
        return filePath.getFileName().toString().replaceAll(".json", "");
    }
    private static Path readFullFileName(final Path filePath){
        return filePath;
    }

    public static Map<String, Map<String, Path>> collectNestedFiles(final Path path) {
        try {
            return Files.list(path)
                    .collect(Collectors.toMap(
                            GroovyHelper::readFileName,
                            GroovyHelper::collectFiles)
                    );
        } catch (final IOException e) {
            GroovyHelper.log.severe("No Fixtures available at Path:" + path);
            return null;
        }
    }

    public static JSONObject parseJsonObject(final String str){
        return new JSONObject(str);
    }

    public static JSONArray parseJsonArray(final String str){
        return new JSONArray(str);
    }

    public static Map getStorage(){
        return GroovyHelper.store;
    }
}
