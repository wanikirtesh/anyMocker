package com.ideas.ngimocker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ideas.ngimocker.components.G3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class G3CallbackService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    G3Client g3Client;

    @Async
    public void callBack1(Object body) throws InterruptedException {
        Thread.sleep(15000);
        logger.info("sending G3 Callback after 15 sec");
        JsonNode nx = new ObjectMapper().valueToTree(body);
        ((ObjectNode)nx).putNull("errors");
        ((ObjectNode)nx).putNull("warnings");
        ((ObjectNode)nx).put("successful",true);
        ((ObjectNode)nx).putObject("response")
                .put("reservationsUpdated", 0)
                .put("groupsUpdated", 0)
                .put("marketSegmentsImpacted",0);
        logger.info("Response is :" + g3Client.sendJobResponse(nx.toString(),nx.get("clientCode").toString().replace("\"",""),nx.get("propertyCode").toString().replace("\"",""),"5"));
    }


}
