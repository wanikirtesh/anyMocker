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
    private final List<Request> requests = new ArrayList<>();
    ConfigurableListableBeanFactory beanFactory;
    @Value("${requests.path}")
    String reqConfigFilePath;
    @Autowired
    public RequestFactory(final ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    @PostConstruct
    private void init() throws IOException {
        this.requests.clear();
        final ObjectMapper mapper = new ObjectMapper();
        RequestFactory.log.info("Mapping requests from directory:" + this.reqConfigFilePath);
        Files.list(Path.of(this.reqConfigFilePath)).forEach(path -> {
            try {
                RequestFactory.log.info("Adding requests from " + path);
                final List<Request> requests = mapper.readValue(new File(path.toString()), new TypeReference<>() {
                });
                final List<Request> aList = requests.stream().peek(x -> x.setFileName(path.toString())).toList();
                this.requests.addAll(aList);
                RequestFactory.log.info("Total " +  this.requests.size() + " requests mapped");
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public List<Request> getRequestList() {
        return requests;
    }

    private String getCaller(final Object o){
        final String[] beanNamesForType = this.beanFactory.getBeanNamesForType(o.getClass());
        if(0 < beanNamesForType.length) {
            return beanNamesForType[0];
        }
        return "";
    }

    public List<Request> getRequests(final RequestProcessor requestProcessor){
        final String caller = this.getCaller(requestProcessor);
        return requests.stream().filter((r)->r.getProcessor().equals(caller)).collect(Collectors.toList());
    }

    public List<Request> getRequests(final String caller){
        //String caller = getCaller(requestProcessor);
        return requests.stream().filter((r)->r.getProcessor().equals(caller)).collect(Collectors.toList());
    }

    public Request getRequest(final String requestName){
        return requests.stream().filter((r)->r.getName().equals(requestName)).findAny().get();
    }


    public void reload() throws IOException {
        this.init();
    }
}
