package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.G3Caller;
import com.ideas.ngimocker.components.MockRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class MockerService {

    @Autowired
    FixturesService fixturesService;

    @Autowired
    G3Caller g3Caller;

    public ResponseEntity<String> processRequest(MockRequest match, Object body) throws Exception {
        if(match != null){
            if(match.getG3CallBack()!=null)
                g3Caller.callBack1(body);
            if(match.isOnlyOK())
                return new ResponseEntity<>(HttpStatus.OK);
            String str = fixturesService.getFixtures(match);
            return new ResponseEntity<>(str, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
