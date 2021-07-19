package com.ideas.ngimocker.service;

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

    public ResponseEntity<String> processRequest(MockRequest match) throws Exception {
        if(match != null){
            String str = fixturesService.getFixtures(match);
            return new ResponseEntity<>(str, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
