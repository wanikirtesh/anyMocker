package com.ideas.ngimocker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas.ngimocker.components.MockRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class MockRequestMapper {

    private List<MockRequest> mockRequests;

    @Value("${requests.path}")
    String reqConfigFilePath;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
            this.mockRequests = mapper.readValue(new File(reqConfigFilePath), new TypeReference<>() {
        });
    }

    public List<MockRequest> getRequestList() {
        return this.mockRequests;
    }
}
