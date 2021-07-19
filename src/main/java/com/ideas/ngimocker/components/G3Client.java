package com.ideas.ngimocker.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class G3Client {
    @Autowired
    HTTPClientWrapper httpClientWrapper;

    private final String g3BaseUrl = "http://mn4sg3xappw101.ideasstg.int:8080";
    public String sendJobResponse(String request,String clientCode,String propertyCode, String propertyId){
        String[] headers = {"Authorization","Basic c3NvQGlkZWFzLmNvbTpwYXNzd29yZA==","Content-Type","application/json"};
        try {
            var str = httpClientWrapper.makePostRequest(
                    g3BaseUrl + "/pacman-platformsecurity/rest/ngi/job/response/?clientCode="+clientCode+"&propertyCode="+propertyCode+"&propertyId=" + propertyId,
                    request, headers);
            System.out.println(str);
            return str;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
}
