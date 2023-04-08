package com.ideas.mocker.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas.mocker.core.components.Request;
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
            this.requests = mapper.readValue(new File(reqConfigFilePath), new TypeReference<>() {
        });
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
