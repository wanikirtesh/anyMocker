package com.ideas.ngimocker.service.processor;

import com.ideas.ngimocker.components.MockRequest;
import com.ideas.ngimocker.service.G3CallbackService;
import com.ideas.ngimocker.service.RequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
@Service("HILTON_TOKEN")
public class HILTONTokenRequestProcessor implements RequestProcessor {
    @Autowired
    G3CallbackService g3CallbackService;
    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        String responseTxt ="{\n\t\"access_token\": \"5c6ee3ac-2c9c-3bed-8d81-0bfa7205fd94.0000016cdee90298.34ac76e31971cf8869e99a2f261533166ea99283\",\n\t\"expires_in\": \"1633\",\n\t\"scope\": \"am_application_scope default\",\n\t\"token_type\": \"Bearer\"\n}";
        headers.set("hltMessageId", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(responseTxt,headers, HttpStatus.OK);
    }

    @Override
    public void postProcessor(MockRequest match, String body, HttpServletRequest req) {
        if(match.getG3CallBack()!=null){
            g3CallbackService.callBack1(body);
        }
    }
}
