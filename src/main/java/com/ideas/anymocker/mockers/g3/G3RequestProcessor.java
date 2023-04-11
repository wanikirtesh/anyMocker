package com.ideas.anymocker.mockers.g3;

import com.ideas.anymocker.core.components.HTTPClient;
import com.ideas.anymocker.core.components.Request;
import com.ideas.anymocker.core.service.RequestProcessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("G3_CRMS")
@Log
public class G3RequestProcessor implements RequestProcessor {
    @Autowired
    HTTPClient httpClient;
    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        log.info(match.getPathParam("batchId"));
        Map<String,String> additionalMeta = new HashMap<>();
        additionalMeta.put("propertyCode",req.getHeader("propertyCode"));
        additionalMeta.put("clientCode",req.getHeader("clientCode"));
        match.addMeta(additionalMeta);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {
        try {
            Thread.sleep(Long.parseLong(match.getMeta("callBackDelay")));
            String propCode = match.getMeta("propertyCode");
            String clientCode = match.getMeta("clientCode");
            String batchId = match.getPathParam("batchId");
            String baseUrl = match.getMeta("callbackURL");
            baseUrl = baseUrl+batchId+"/"+propCode;
            String[] headers = new String[4];
            headers[0] = "content-type";
            headers[1] = "application/json";
            headers[2] = "authorization";
            String strToken = generateToken(match.getMeta());
            headers[3] = strToken;
            String strBody = "{ \"batchId\": \""+batchId+"\", \"jobInstanceId\": "+new Date().getTime() +", \"clientCode\": \""+clientCode+"\", \"propertyCode\": \""+propCode+"\", \"status\": \"Completed\", \"message\": \"Completed\" }";
            httpClient.makePostRequest(baseUrl,strBody,headers);

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String generateToken(Map<String, String> meta) {
        log.info("generating access token");
        String[] httpHeaders = new String[4];
        httpHeaders[0] = "content-type";
        httpHeaders[1] = "application/x-www-form-urlencoded";
        httpHeaders[2] = "authorization";
        httpHeaders[3] = meta.get("tokenAuth");
        HttpResponse<String> response = httpClient.makePostRequest(meta.get("tokenUrl"), meta.get("tokenBody"), httpHeaders);
        var rsObj = new JSONObject(response.body());
        return rsObj.get("token_type")+" " + rsObj.get("access_token");
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void downloadFixtures(Request match) {

    }
}
