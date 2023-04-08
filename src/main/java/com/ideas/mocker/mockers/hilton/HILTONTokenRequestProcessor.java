package com.ideas.mocker.mockers.hilton;

import com.ideas.mocker.core.components.Request;
import com.ideas.mocker.core.service.RequestProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
@Service("HILTON_TOKEN")
public class HILTONTokenRequestProcessor implements RequestProcessor {


    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        String responseTxt ="{\n\t\"access_token\": \"5c6ee3ac-2c9c-3bed-8d81-0bfa7205fd94.0000016cdee90298.34ac76e31971cf8869e99a2f261533166ea99283\",\n\t\"expires_in\": \"1633\",\n\t\"scope\": \"am_application_scope default\",\n\t\"token_type\": \"Bearer\"\n}";
        headers.set("hltMessageId", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(responseTxt,headers, HttpStatus.OK);
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void downloadFixtures(Request match) {

    }
}
