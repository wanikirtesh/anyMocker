package com.wanikirtesh.anymocker.core.components;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.newBuilder;

@Component
@Log
public class HTTPClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String makeGetRequest(String uri) {
        return makeRequest(uri, "GET", HttpRequest.BodyPublishers.noBody());
    }

    public String makePostRequest(String uri, HttpRequest.BodyPublisher bodyPublisher) {
        return makeRequest(uri, "POST", bodyPublisher);
    }
    public HttpResponse<String> makePostRequest(String uri, String data, String[] headers) {
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
            throw new RuntimeException(e);
        }
        log.info("Response code for the request is:" + response.statusCode());
        return response;
    }

    private String makeRequest(String uri, String method, HttpRequest.BodyPublisher body) {
        try {
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
}
