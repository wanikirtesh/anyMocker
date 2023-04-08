package com.ideas.ngimocker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ideas.ngimocker.components.G3Client;
import com.ideas.ngimocker.components.HTTPClientWrapper;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpResponse;

@Component
@Log
public class G3CallbackService {
    @Autowired
    G3Client g3Client;
    @Autowired
    HTTPClientWrapper httpClient;
    public void callBack1(Object body) {
           log.info("sending G3 Callback");
            JsonNode nx = new ObjectMapper().valueToTree(body);
            ((ObjectNode) nx).putNull("errors");
            ((ObjectNode) nx).putNull("warnings");
            ((ObjectNode) nx).put("successful", true);
            ((ObjectNode) nx).putObject("response")
                    .put("reservationsUpdated", 0)
                    .put("groupsUpdated", 0)
                    .put("marketSegmentsImpacted", 0);
            log.info("Response is :" + g3Client.sendJobResponse(nx.toString(), nx.get("clientCode").toString().replace("\"", ""), nx.get("propertyCode").toString().replace("\"", ""), "5"));
    }

    public void callBackHTNG(String[] callBack, String contentType){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String[] headers = new String[2];
        headers[0]="Content-Type";
        headers[1] = contentType ;
        HttpResponse<String> response = httpClient.makePostRequest(callBack[1], callBack[0], headers);
        if(response.statusCode()>300){
            log.severe(callBack[0]);
        }
    }


}
