package com.ideas.anymocker.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas.anymocker.core.components.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Controller;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Log
public class RequestFactory {
    private List<Request> requests;
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
        log.info("Mapping requests from file:" + reqConfigFilePath);
        this.requests = mapper.readValue(new File(reqConfigFilePath), new TypeReference<>() {
        });
        log.info("Total " +  requests.size() + " requests mapped");
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
}
