package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
@Log
public class MockerService {

    @Autowired
    FixtureRequestProcessor fixturesService;

    @Autowired
    HTNGDecisionRequestProcessor htngDecisionService;

    @Autowired
    G3CallbackService g3CallbackService;
    @Autowired
    OXIDecisionRequestProcessor oxi;
    @Autowired
    HILTONTokenRequestProcessor hiltonTokenRequestProcessor;

    private final Map<String, RequestProcessor> processorMap = new HashMap<>();
    @PostConstruct
    private void loadProcessMap(){
        processorMap.put("HTNG", htngDecisionService);
        processorMap.put("FIXTURE",fixturesService);
        processorMap.put("OXI",oxi);
        processorMap.put("HILTON_TOKEN",hiltonTokenRequestProcessor);
    }

    public ResponseEntity<String> processRequest(MockRequest match, String body, HttpServletRequest req) throws Exception {
        if(match != null){
            if(match.getG3CallBack()!=null){
                g3CallbackService.callBack1(body);
            }
            return match.getStore().processRequest(match,body,processorMap,req);
        }
        return new ResponseEntity<>("success",HttpStatus.OK);
    }
}
