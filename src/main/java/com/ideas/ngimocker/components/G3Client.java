package com.ideas.ngimocker.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class G3Client {
    @Autowired
    HTTPClientWrapper httpClientWrapper;

    @Value("${g3.url}")
    String g3BaseUrl;

    public String sendJobResponse(String request,String clientCode,String propertyCode, String propertyId){
        String[] headers = {"Authorization","Basic c3NvQGlkZWFzLmNvbTpwYXNzd29yZA==","Content-Type","application/json"};
        try {
            var str = httpClientWrapper.makePostRequest(
                    g3BaseUrl + "/pacman-platformsecurity/rest/ngi/job/response/?clientCode="+clientCode+"&propertyCode="+propertyCode+"&propertyId=" + propertyId,
                    request, headers);
            System.out.println(str);
            return str;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
}
