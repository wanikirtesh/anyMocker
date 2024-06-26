package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.Request;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class RequestFactory {
    private List<Request> requests = new ArrayList<>();
    ConfigurableListableBeanFactory beanFactory;
    @Value("${requests.path}")
    String reqConfigFilePath;
    @Autowired
    public RequestFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    @PostConstruct
    private void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        log.info("Mapping requests from directory:" + reqConfigFilePath);
        Files.list(Path.of(reqConfigFilePath)).forEach(path -> {
            try {
                log.info("Adding requests from " + path);
                this.requests.addAll(mapper.readValue(new File(path.toString()), new TypeReference<>() {
                }));
                log.info("Total " +  requests.size() + " requests mapped");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
       // this.requests = mapper.readValue(new File(reqConfigFilePath), new TypeReference<>() {
       // });

    }
    List<Request> getRequestList() {
        return this.requests;
    }

    private String getCaller(Object o){
        String[] beanNamesForType = beanFactory.getBeanNamesForType(o.getClass());
        if(beanNamesForType.length>0) {
            return beanNamesForType[0];
        }
        return "";
    }

    public List<Request> getRequests(RequestProcessor requestProcessor){
        String caller = getCaller(requestProcessor);
        return this.requests.stream().filter((r)->r.getProcessor().equals(caller)).collect(Collectors.toList());
    }

    public List<Request> getRequests(String caller){
        //String caller = getCaller(requestProcessor);
        return this.requests.stream().filter((r)->r.getProcessor().equals(caller)).collect(Collectors.toList());
    }
}
