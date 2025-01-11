package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UIController {
    @Value("${requests.grouping}")
    Boolean groupRequests;
    @Value("${processors.path}")
    private String processorsPath;

    @Autowired
    private RequestFactory requestFactory;

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE")
    public String manager(final Model model) {
        model.addAttribute("groupedUrls", groupRequests(this.requestFactory.getRequestList()));
        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/NEW")
    public String manager_new(final Model model) {
        model.addAttribute("groupedUrls", groupRequests(this.requestFactory.getRequestList()));
        return "index_new";
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
        final String content = Files.readString(Paths.get(this.processorsPath, file + ".groovy"));
        model.addAttribute("content", content);
        model.addAttribute("fileName", file);
        return "fragment/editor";
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
        return "fragment/tree";
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


    private Map<String, Map<String, Map<String, List<Request>>>> groupRequests(List<Request> requestList) {
        Map<String, Map<String, Map<String, List<Request>>>> resultMap = new HashMap<>();
        for (Request request : requestList) {
            String[] parts = request.getUrl().split("/");
            if (4 <= parts.length && request.isGrouping() && groupRequests) {
                String product = parts[1];
                String module = parts[2];
                String persona = parts[3];
                resultMap
                        .computeIfAbsent(product, k -> new HashMap<>())
                        .computeIfAbsent(module, k -> new HashMap<>())
                        .computeIfAbsent(persona, k -> new ArrayList<>())
                        .add(request);
            } else {
                resultMap
                        .computeIfAbsent("PRODUCT", k -> new HashMap<>())
                        .computeIfAbsent("MODULE", k -> new HashMap<>())
                        .computeIfAbsent("PERSONA", k -> new ArrayList<>())
                        .add(request);
            }
        }
        return resultMap;
    }

}
