package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class UIController {
    @Value("${processors.path}")
    private String processorsPath;

    @Value("${specs.path}")
    private String specsPath;


    @Autowired
    private RequestFactory requestFactory;

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE")
    public String manager_new(final Model model) {
        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/REQUEST/GET/V2/{request}")
    public String getRequestV2(final Model model, @PathVariable final String request) {
        Request r = requestFactory.getRequest(request);
        model.addAttribute("req", r);
        model.addAttribute("add", false);
        model.addAttribute("pre", "");
        return "fragment/request";
    }
    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/PROCESSOR/{file}")
    public String getProcessor(final Model model,@PathVariable final String file) throws IOException {
        try {
            final String content = Files.readString(Paths.get(this.processorsPath, file + ".groovy"));
            model.addAttribute("content", content);
            model.addAttribute("fileName", file);
            model.addAttribute("isProcessor", true);
            return "fragment/editor";
        }catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/SPEC/{file}")
    public String getSpec(final Model model,@PathVariable final String file) throws IOException {
        try {
            final String content = Files.readString(Paths.get(this.specsPath, file));
            model.addAttribute("content", content);
            model.addAttribute("fileName", file);
            model.addAttribute("isProcessor", false);
            return "fragment/editor";
        }catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/MOCKER/MANAGE/REQUEST/NEW/V2")
    public String getRequestV2New(final Model model, @RequestParam(required = false) final String isNew, @RequestParam(required = false) String fileName, @RequestBody(required = false) Request oldR) {
        Request r;
        String pre = "ed_";
        if (isNew != null && isNew.equals("true")) {
            r = new Request();
            r.setFileName("");
        } else {
            r = oldR;
            r.setFileName(fileName);
        }
        model.addAttribute("req", r);
        model.addAttribute("add", true);
        model.addAttribute("pre", pre);
        return "fragment/request";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/REQUEST/GET/ALL")
    public String getTree(final Model model) {
        model.addAttribute("groupedUrls", groupRequestsNew(this.requestFactory.getRequestList()));
        model.addAttribute("processors", getProcessors(this.requestFactory.getRequestList()));
        model.addAttribute("specifications",getSpecifications(this.requestFactory.getRequestList()));
        return "fragment/tree";
    }

    private Set<String> getSpecifications(List<Request> requestList) {
        return requestList.stream().filter(r -> !r.getSpec().isBlank()).map(Request::getSpec).collect(Collectors.toSet());
    }

    private Map<String, List<Request>> groupRequestsNew(final List<Request> requestList) {
        Map<String, List<Request>> resultMap = new HashMap<>();
        for (Request request : requestList) {
            resultMap.computeIfAbsent(request.getFileName().replace(".json", ""), k -> new ArrayList<>())
                    .add(request);

        }
        return resultMap;
    }

    private Set<String> getProcessors(final List<Request> requestList) {
        return requestList.stream().map(Request::getProcessor).collect(Collectors.toSet());
    }
}
