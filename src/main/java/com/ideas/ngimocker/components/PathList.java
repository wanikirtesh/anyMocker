package com.ideas.ngimocker.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class PathList {

    private List<MockRequest> mockRequests;

    @Value("${requests.path}")
    String reqConfigFilePath;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
            this.mockRequests = mapper.readValue(new File(reqConfigFilePath), new TypeReference<>() {
        });
    }

   /* public static PathList getPathList() throws IOException {
       if(pathList!=null){
           return pathList;
       }
       return new PathList();
    }

    */

    public List<MockRequest> get() {
        return this.mockRequests;
    }
}
