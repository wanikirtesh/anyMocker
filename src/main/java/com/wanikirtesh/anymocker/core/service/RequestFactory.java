package com.wanikirtesh.anymocker.core.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.wanikirtesh.anymocker.core.components.Request;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RequestFactory {
    private List<Request> requests = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    ConfigurableListableBeanFactory beanFactory;
    @Value("${requests.path}")
    String reqConfigFilePath;

    @Autowired
    public RequestFactory(final ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    private void init() throws IOException {
        RequestFactory.log.info("Mapping requests from directory:" + this.reqConfigFilePath);
        requests.clear();
        Files.list(Path.of(this.reqConfigFilePath)).forEach(path -> {
            try {
                RequestFactory.log.info("Adding requests from " + path);
                final List<Request> requests = this.getListFromFile(new File(path.toString()));
                final List<Request> aList = requests.stream().peek(x -> x.setFileName(path.getFileName().toString())).toList();
                this.requests.addAll(aList);
                RequestFactory.log.info("Total " + this.requests.size() + " requests mapped");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Request> getRequestList() {
        return requests;
    }

    private String getCaller(final Object o) {
        final String[] beanNamesForType = this.beanFactory.getBeanNamesForType(o.getClass());
        if (0 < beanNamesForType.length) {
            return beanNamesForType[0];
        }
        return "";
    }

    public List<Request> getRequests(final RequestProcessor requestProcessor) {
        final String caller = getCaller(requestProcessor);
        return requests.stream().filter((r) -> r.getProcessor().equals(caller)).collect(Collectors.toList());
    }

    public List<Request> getRequests(final String caller) {
        //String caller = getCaller(requestProcessor);
        return requests.stream().filter((r) -> r.getProcessor().equals(caller)).collect(Collectors.toList());
    }

    public Request getRequest(final String requestName) {
        return requests.stream().filter((r) -> r.getName().equals(requestName)).findAny().get();
    }


    public void reload() throws IOException {
        this.init();
    }

    public void saveRequest(Request request) throws IOException {
        final String fileName = Path.of(request.getFileName().replace(".json", "") + ".json").getFileName().toString();
        final Path filePath = Path.of(reqConfigFilePath, fileName);
        File fl = new File(filePath.toString());
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        final List<Request> requests = this.getListFromFile(fl);
        requests.removeIf(rq -> rq.getName().equals(request.getName()));
        requests.add(request);
        RequestFactory.mapper.writeValue(fl, requests);
        reload();
    }

    private List<Request> getListFromFile(File file) {
        try {
            return RequestFactory.mapper.readValue(file, new TypeReference<>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public void saveRequest(Request request, String requestName) throws IOException {
        final String fileName = Path.of(request.getFileName().replace(".json", "") + ".json").getFileName().toString();
        final Path filePath = Path.of(reqConfigFilePath, fileName);
        File fl = new File(filePath.toString());
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        final List<Request> requests = this.getListFromFile(fl);
        requests.removeIf(rq -> rq.getName().equals(requestName));
        requests.add(request);
        RequestFactory.mapper.writeValue(fl, requests);
        reload();
    }

    public void deleteRequest(String requestName, String file_Name) throws IOException {
        final String fileName = Path.of(file_Name.replace(".json", "") + ".json").getFileName().toString();
        final Path filePath = Path.of(reqConfigFilePath, fileName);
        File fl = new File(filePath.toString());
        final List<Request> requests = this.getListFromFile(fl);
        requests.removeIf(rq -> rq.getName().equals(requestName));
        RequestFactory.mapper.writeValue(fl, requests);
        reload();
    }
}
